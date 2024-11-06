package server.filter;


import ace.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;

//@Order(1)
//@Component
@WebFilter(urlPatterns = {"/customers/*"})
public class CustomerJsonSchemaValidatorFilter extends JsonSchemaValidatorFilterBase implements Filter
{
    private JsonSchema _jsonSchema;
    private static final ObjectMapper _objMapper = Utils.getObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        if (_jsonSchema == null)
        {
            this._jsonSchema = this.loadSchema();
        }
        String requestMethod = ((HttpServletRequest) request).getMethod();
        if (requestMethod.equalsIgnoreCase("GET") || requestMethod.equalsIgnoreCase("DELETE"))
        {
            chain.doFilter(request, response);
        }
        try
        {
            JsonNode jsonNode = _objMapper.readTree(request.getInputStream());
            Set<ValidationMessage> validationMessages = _jsonSchema.validate(jsonNode);
            if (validationMessages.isEmpty())
            {
                chain.doFilter(request, response);
            }
            else
            {
                this.rejectRequest(((HttpServletResponse) response), "Invalid customer data provided");
            }
        }
        catch (Exception e)
        {
            this.rejectRequest(((HttpServletResponse) response), "Invalid customer data provided");
        }
    }

    @Override
    JsonSchema loadSchema()
    {
        InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("schemas/customer.json");
        if (schemaStream == null) {
            throw new IllegalArgumentException("Schema file not found");
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        return factory.getSchema(schemaStream);
    }

    private void rejectRequest(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setContentType("application/json");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"message\": \"" + message + "\"}");
        }
    }
}
