import api from "./api";

export async function createOrder(orderRequest) {
  const response = await api.post("/v1/orders", orderRequest);
  return response.data;
}
