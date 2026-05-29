import { useEffect, useMemo, useState } from "react";
import { getOrders, updateActualWeights } from "../../services/orderService";
import { formatCurrency } from "../../utils/formatCurrency";

function Dashboard() {
  const [orders, setOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [actualWeights, setActualWeights] = useState({});
  const [message, setMessage] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const selectedItems = useMemo(
    () => selectedOrder?.items || [],
    [selectedOrder],
  );

  async function loadPendingOrders() {
    setIsLoading(true);
    try {
      const apiResponse = await getOrders("PENDING");
      const pendingOrders = apiResponse.data || [];
      setOrders(pendingOrders);

      if (
        selectedOrder &&
        !pendingOrders.some((order) => order.id === selectedOrder.id)
      ) {
        setSelectedOrder(null);
        setActualWeights({});
      }
    } catch (error) {
      const errorMessage =
        error?.response?.data?.message ||
        error.message ||
        "Khong the tai danh sach don hang.";
      setMessage({ type: "error", text: errorMessage });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadPendingOrders();
  }, []);

  const handleSelectOrder = (order) => {
    setMessage(null);
    setSelectedOrder(order);
    setActualWeights(
      Object.fromEntries(
        (order.items || []).map((item) => [
          item.id,
          item.actualWeight ?? item.orderedWeight ?? "",
        ]),
      ),
    );
  };

  const handleWeightChange = (orderItemId, value) => {
    setActualWeights((prev) => ({
      ...prev,
      [orderItemId]: value,
    }));
  };

  const handleSubmitWeights = async () => {
    if (!selectedOrder) {
      return;
    }

    const items = selectedItems.map((item) => ({
      orderItemId: item.id,
      productId: item.productId,
      actualWeight: Number(actualWeights[item.id]),
    }));

    if (items.some((item) => !item.actualWeight || item.actualWeight <= 0)) {
      setMessage({
        type: "error",
        text: "Vui long nhap so can thuc te hop le cho tung san pham.",
      });
      return;
    }

    try {
      setIsSubmitting(true);
      const apiResponse = await updateActualWeights(selectedOrder.id, items);
      setMessage({
        type: "success",
        text:
          apiResponse.message ||
          `Da xu ly xong don ${selectedOrder.orderCode}.`,
      });
      setSelectedOrder(null);
      setActualWeights({});
      await loadPendingOrders();
    } catch (error) {
      const errorMessage =
        error?.response?.data?.message ||
        error.message ||
        "Khong the cap nhat can nang thuc te.";
      setMessage({ type: "error", text: errorMessage });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-semibold text-slate-900">
        Dashboard Kho
      </h1>
      <p className="mt-4 text-slate-600">
        Danh sach don hang dang cho can va xu ly tru kho.
      </p>

      {message && (
        <div
          className={`mt-6 rounded-lg border px-4 py-3 text-sm ${
            message.type === "success"
              ? "border-emerald-300 bg-emerald-50 text-emerald-800"
              : "border-red-300 bg-red-50 text-red-700"
          }`}
        >
          {message.text}
        </div>
      )}

      <section className="mt-8 grid gap-6 lg:grid-cols-[1.4fr_1fr]">
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <h2 className="text-xl font-semibold text-slate-900">
              Don hang cho can
            </h2>
            <button
              type="button"
              onClick={loadPendingOrders}
              disabled={isLoading}
              className="rounded-full border border-slate-300 px-5 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400"
            >
              {isLoading ? "Dang tai..." : "Tai lai"}
            </button>
          </div>

          <div className="mt-6 space-y-4">
            {isLoading && (
              <p className="text-slate-600">Dang tai danh sach don hang...</p>
            )}

            {!isLoading && orders.length === 0 && (
              <p className="text-slate-600">Khong co don hang PENDING.</p>
            )}

            {orders.map((order) => (
              <article
                key={order.id}
                className="rounded-2xl border border-slate-200 p-4"
              >
                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-slate-900">
                      {order.orderCode}
                    </h3>
                    <p className="mt-1 text-sm text-slate-600">
                      Tong tam tinh: {formatCurrency(order.totalAmount)}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleSelectOrder(order)}
                    className="rounded-full bg-sky-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-sky-700"
                  >
                    Xu ly
                  </button>
                </div>

                <div className="mt-4 space-y-2">
                  {(order.items || []).map((item) => (
                    <div
                      key={item.id}
                      className="rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-700"
                    >
                      <span className="font-semibold text-slate-900">
                        {item.productName}
                      </span>{" "}
                      - du kien {item.orderedWeight} KG
                    </div>
                  ))}
                </div>
              </article>
            ))}
          </div>
        </div>

        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <h2 className="text-xl font-semibold text-slate-900">
            Nhap can thuc te
          </h2>

          {!selectedOrder ? (
            <p className="mt-4 text-slate-600">
              Chon mot don hang de bat dau xu ly.
            </p>
          ) : (
            <div className="mt-6 space-y-4">
              <div className="rounded-2xl bg-slate-50 p-4">
                <p className="text-sm text-slate-600">Don dang xu ly</p>
                <p className="mt-1 font-semibold text-slate-900">
                  {selectedOrder.orderCode}
                </p>
              </div>

              {selectedItems.map((item) => (
                <label
                  key={item.id}
                  className="block rounded-2xl border border-slate-200 p-4"
                >
                  <span className="font-semibold text-slate-900">
                    {item.productName}
                  </span>
                  <span className="mt-1 block text-sm text-slate-600">
                    Du kien: {item.orderedWeight} KG
                  </span>
                  <input
                    type="number"
                    min="0.1"
                    step="0.1"
                    value={actualWeights[item.id] ?? ""}
                    onChange={(event) =>
                      handleWeightChange(item.id, event.target.value)
                    }
                    className="mt-3 w-full rounded-xl border border-slate-300 px-4 py-2 text-slate-900 outline-none focus:border-sky-500"
                  />
                </label>
              ))}

              <button
                type="button"
                onClick={handleSubmitWeights}
                disabled={isSubmitting}
                className="w-full rounded-full bg-emerald-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:bg-slate-400"
              >
                {isSubmitting ? "Dang cap nhat..." : "Xac nhan can va tru kho"}
              </button>
            </div>
          )}
        </div>
      </section>
    </main>
  );
}

export default Dashboard;
