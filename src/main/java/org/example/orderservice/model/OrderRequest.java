package org.example.orderservice.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Setter
@Getter
@Builder
@Data
@JacksonXmlRootElement(localName = "order")
public class OrderRequest {
  //  private String productId;
   // public String getProductId() { return productId; }
 //   public void setProductId(String productId) { this.productId = productId; }

        private String orderId;
        private String orderType;  // e.g., digital, in-store
        private String storeId;
        private Map<String, Object> details; // flexible metadata

        public OrderRequest(String orderId, String orderType, String storeId, Map<String, Object> details) {
                this.orderId = orderId;
                this.orderType = orderType;
                this.storeId = storeId;
                this.details = details;
        }

        public String getOrderId() {
                return orderId;
        }

        public void setOrderId(String orderId) {
                this.orderId = orderId;
        }

        public String getOrderType() {
                return orderType;
        }

        public void setOrderType(String orderType) {
                this.orderType = orderType;
        }

        public String getStoreId() {
                return storeId;
        }

        public void setStoreId(String storeId) {
                this.storeId = storeId;
        }

        public Map<String, Object> getDetails() {
                return details;
        }

        public void setDetails(Map<String, Object> details) {
                this.details = details;
        }
}

