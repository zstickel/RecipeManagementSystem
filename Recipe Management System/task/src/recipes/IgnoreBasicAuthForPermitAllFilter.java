package recipes;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class IgnoreBasicAuthForPermitAllFilter extends BasicAuthenticationFilter {

    public IgnoreBasicAuthForPermitAllFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestURI = request.getRequestURI();
        // Add your permitAll() endpoint URI here
        if (requestURI.equals("/api/register")) {
            return true;
        }
        return super.shouldNotFilter(request);
    }
}