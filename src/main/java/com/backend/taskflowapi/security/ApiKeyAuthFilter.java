package com.backend.taskflowapi.security;

import com.backend.taskflowapi.dto.response.ErrorResponse;
import com.backend.taskflowapi.entity.ApiKey;
import com.backend.taskflowapi.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-Api-Key";
    private static final Pattern ORG_PATH =
            Pattern.compile("^/api/v\\d+/organizations/(\\d+)(/.*)?$");

    private final ApiKeyRepository apiKeyRepository;
    private final ApiKeyGenerator apiKeyGenerator;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository,  ApiKeyGenerator apiKeyGenerator, ObjectMapper objectMapper) {
        this.apiKeyRepository = apiKeyRepository;
        this.apiKeyGenerator = apiKeyGenerator;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String rawKey = request.getHeader(API_KEY_HEADER);

        if (rawKey == null || SecurityContextHolder.getContext().getAuthentication() != null){
            filterChain.doFilter(request, response);
            return;
        }

        Optional<ApiKey> found = apiKeyRepository.findActiveByKeyHash(apiKeyGenerator.hash(rawKey));

        if (found.isEmpty() || !found.get().getCreatedBy().isEnabled()){
            writeError(request, response, HttpStatus.UNAUTHORIZED, "Invalid or revoked API key");
            return;
        }

        ApiKey apiKey = found.get();

        if (!isAllowedPath(request.getRequestURI(), apiKey.getOrganization().getId())){
            writeError(request, response, HttpStatus.FORBIDDEN, "This API key cannot access this resource");
        }

        CustomUserDetails userDetails = new CustomUserDetails(apiKey.getCreatedBy());

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        touchLastUsed(apiKey);

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path, Long keyOrganizationId){
        Matcher matcher = ORG_PATH.matcher(path);

        if (!matcher.matches()){
            return false;
        }

        boolean sameOrganization = matcher.group(1).equals(String.valueOf(keyOrganizationId));
        boolean touchesApiKeys = path.contains("/api-keys");

        return sameOrganization && !touchesApiKeys;
    }

    private void touchLastUsed(ApiKey apiKey){
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = apiKey.getLastUsedAt();

        if (last == null || last.isBefore(now.minusMinutes(5))){
            apiKey.setLastUsedAt(now);
            apiKeyRepository.save(apiKey);
        }
    }

    private void writeError(
            HttpServletRequest request, HttpServletResponse response,
            HttpStatus status, String message
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
