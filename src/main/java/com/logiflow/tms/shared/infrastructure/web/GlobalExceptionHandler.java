package com.logiflow.tms.shared.infrastructure.web;

import com.logiflow.tms.shared.domain.exception.BusinessException;
import com.logiflow.tms.shared.domain.exception.ConflictException;
import com.logiflow.tms.shared.domain.exception.NotFoundException;
import com.logiflow.tms.shared.domain.exception.ServiceIndisponibleException;
import com.logiflow.tms.shared.domain.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Traduction centralisée des exceptions en réponses RFC 7807 ({@link ProblemDetail}). Aucun détail
 * technique n'est exposé au client pour les erreurs non anticipées : seul l'identifiant de
 * corrélation permet de retrouver la trace complète dans les logs.
 *
 * <p>Les messages génériques (sans paramètre dynamique) sont externalisés dans {@code
 * messages_fr.properties} / {@code messages_en.properties} et résolus via {@link MessageSource}.
 * Les messages métier portant un identifiant (site/client introuvable, code déjà utilisé...) sont
 * construits directement par la couche application, au plus près du contexte.
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final MessageSource messageSource;

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail gererRegleMetier(BusinessException ex, HttpServletRequest request) {
    return ApiError.of(
        HttpStatus.UNPROCESSABLE_ENTITY,
        "regle-metier",
        "Règle métier non respectée",
        ex.getMessage(),
        request);
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail gererIntrouvable(NotFoundException ex, HttpServletRequest request) {
    return ApiError.of(
        HttpStatus.NOT_FOUND,
        "ressource-introuvable",
        "Ressource introuvable",
        ex.getMessage(),
        request);
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail gererConflit(ConflictException ex, HttpServletRequest request) {
    return ApiError.of(HttpStatus.CONFLICT, "conflit", "Conflit d'état", ex.getMessage(), request);
  }

  @ExceptionHandler(ValidationException.class)
  public ProblemDetail gererValidationMetier(ValidationException ex, HttpServletRequest request) {
    ProblemDetail problemDetail =
        ApiError.of(
            HttpStatus.BAD_REQUEST, "validation", "Requête invalide", ex.getMessage(), request);
    problemDetail.setProperty("violations", ex.violations());
    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail gererValidationBean(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    var violations =
        ex.getBindingResult().getFieldErrors().stream()
            .map(erreur -> "%s: %s".formatted(erreur.getField(), erreur.getDefaultMessage()))
            .toList();
    ProblemDetail problemDetail =
        ApiError.of(
            HttpStatus.BAD_REQUEST,
            "validation",
            "Requête invalide",
            messageDe("error.validation.generic"),
            request);
    problemDetail.setProperty("violations", violations);
    return problemDetail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail gererArgumentInvalide(
      IllegalArgumentException ex, HttpServletRequest request) {
    return ApiError.of(
        HttpStatus.BAD_REQUEST, "argument-invalide", "Requête invalide", ex.getMessage(), request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail gererCorpsIllisible(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return ApiError.of(
        HttpStatus.BAD_REQUEST,
        "corps-illisible",
        "Corps de requête invalide",
        messageDe("error.request.illisible"),
        request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail gererParametreManquant(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    return ApiError.of(
        HttpStatus.BAD_REQUEST,
        "parametre-manquant",
        "Requête invalide",
        ex.getMessage(),
        request);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail gererRessourceAbsente(
      NoResourceFoundException ex, HttpServletRequest request) {
    return ApiError.of(
        HttpStatus.NOT_FOUND,
        "ressource-introuvable",
        "Ressource introuvable",
        messageDe("error.notfound.generic"),
        request);
  }

  @ExceptionHandler(ServiceIndisponibleException.class)
  public ProblemDetail gererServiceIndisponible(
      ServiceIndisponibleException ex, HttpServletRequest request) {
    log.warn(
        "Service externe indisponible [correlationId={}] : {}",
        CorrelationIdFilter.correlationIdCourant(),
        ex.getMessage());
    return ApiError.of(
        HttpStatus.SERVICE_UNAVAILABLE,
        "service-indisponible",
        "Service temporairement indisponible",
        ex.getMessage(),
        request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail gererAccesRefuse(AccessDeniedException ex, HttpServletRequest request) {
    return ApiError.of(
        HttpStatus.FORBIDDEN,
        "acces-refuse",
        "Accès refusé",
        messageDe("error.access.denied"),
        request);
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail gererErreurInattendue(Exception ex, HttpServletRequest request) {
    log.error(
        "Erreur interne non gérée [correlationId={}]",
        CorrelationIdFilter.correlationIdCourant(),
        ex);
    return ApiError.of(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "erreur-interne",
        "Erreur interne",
        messageDe("error.internal"),
        request);
  }

  private String messageDe(String code) {
    return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
  }
}
