package org.example.orderservice.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.orderservice.model.OrderRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
public class OrderTransformerService {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    /**
     * Transform raw payload (JSON or XML string) into internal OrderRequest DTO.
     *
     * @param rawPayload raw order data as String
     * @param contentType content type header ("application/json" or "application/xml")
     * @return OrderRequest populated with extracted fields
     * @throws IOException parsing errors
     */
    public OrderRequest transform(String rawPayload, String contentType) throws IOException {
        if (contentType == null) {
            throw new IllegalArgumentException("Content-Type cannot be null");
        }

        if (contentType.contains("json")) {
            return transformFromJson(rawPayload);
        } else if (contentType.contains("xml")) {
            return transformFromXml(rawPayload);
        } else {
            throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
        }
    }

    private OrderRequest transformFromJson(String jsonPayload) throws IOException {
        JsonNode root = jsonMapper.readTree(jsonPayload);

        // Extract fields - customize based on your expected JSON structure
        String orderId = root.path("orderId").asText();
        String orderType = root.path("orderType").asText();
        String storeId = root.path("storeId").asText();

        // Extract 'details' object as a Map<String, Object>
        Map<String, Object> details = new HashMap<>();
        JsonNode detailsNode = root.path("details");
        if (!detailsNode.isMissingNode()) {
            Iterator<Map.Entry<String, JsonNode>> fields = detailsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                details.put(field.getKey(), jsonMapper.treeToValue(field.getValue(), Object.class));
            }
        }

        return new OrderRequest(orderId, orderType, storeId, details);
    }

    private OrderRequest transformFromXml(String xmlPayload) throws IOException {
        JsonNode root = xmlMapper.readTree(xmlPayload);

        // Example XML structure assumed:
        // <Order>
        //   <Header>
        //     <Id>12345</Id>
        //     <Type>digital</Type>
        //     <Location>Store123</Location>
        //   </Header>
        //   <Details>
        //     <Item>...</Item>
        //     ...
        //   </Details>
        // </Order>

        JsonNode header = root.path("Header");

        String orderId = header.path("Id").asText();
        String orderType = header.path("Type").asText();
        String storeId = header.path("Location").asText();

        Map<String, Object> details = new HashMap<>();
        JsonNode detailsNode = root.path("Details");

        if (!detailsNode.isMissingNode()) {
            Iterator<Map.Entry<String, JsonNode>> fields = detailsNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                details.put(field.getKey(), xmlMapper.treeToValue(field.getValue(), Object.class));
            }
        }

        return new OrderRequest(orderId, orderType, storeId, details);
    }
}


