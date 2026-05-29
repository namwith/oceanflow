import api from "./api";

export async function getOrders(status) {
  const response = await api.get("/orders", {
    params: status ? { status } : undefined,
  });
  return response.data;
}

export async function createOrder(orderRequest) {
  const response = await api.post("/orders", orderRequest);
  return response.data;
}

export async function updateActualWeights(orderId, items) {
  const response = await api.put(`/orders/${orderId}/actual-weights`, {
    items,
  });
  return response.data;
}
