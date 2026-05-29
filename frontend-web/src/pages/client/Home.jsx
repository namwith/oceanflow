import React, { useEffect, useMemo, useState } from "react";
import { createOrder } from "../../services/orderService";
import { getProducts } from "../../services/productService";
import { formatCurrency } from "../../utils/formatCurrency";

function Home() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [paymentMethod, setPaymentMethod] = useState("COD");
  const [message, setMessage] = useState(null);
  const [isLoadingProducts, setIsLoadingProducts] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    async function loadProducts() {
      try {
        const apiResponse = await getProducts();
        setProducts(apiResponse.data || []);
      } catch (error) {
        const errorMessage =
          error?.response?.data?.message ||
          error.message ||
          "Không thể tải danh sách sản phẩm.";
        setMessage({ type: "error", text: errorMessage });
      } finally {
        setIsLoadingProducts(false);
      }
    }

    loadProducts();
  }, []);

  const cartTotal = useMemo(
    () => cart.reduce((sum, item) => sum + item.subtotal, 0),
    [cart],
  );

  const handleAddToCart = (product) => {
    setMessage(null);
    const pricePerUnit = product.basePrice || 0;

    setCart((prevCart) => {
      const existing = prevCart.find((item) => item.productId === product.id);
      if (existing) {
        return prevCart.map((item) =>
          item.productId === product.id
            ? {
                ...item,
                orderedWeight: item.orderedWeight + 0.5,
                subtotal: (item.orderedWeight + 0.5) * item.pricePerUnit,
              }
            : item,
        );
      }

      return [
        ...prevCart,
        {
          productId: product.id,
          name: product.name,
          orderedWeight: 0.5,
          pricePerUnit,
          subtotal: 0.5 * pricePerUnit,
        },
      ];
    });
  };

  const handleWeightChange = (productId, value) => {
    setMessage(null);
    const updatedWeight = Number(value);
    setCart((prevCart) =>
      prevCart.map((item) =>
        item.productId === productId
          ? {
              ...item,
              orderedWeight: updatedWeight,
              subtotal: updatedWeight * item.pricePerUnit,
            }
          : item,
      ),
    );
  };

  const handleRemoveItem = (productId) => {
    setMessage(null);
    setCart((prevCart) =>
      prevCart.filter((item) => item.productId !== productId),
    );
  };

  const handleCheckout = async () => {
    setMessage(null);

    if (cart.length === 0) {
      setMessage({
        type: "error",
        text: "Giỏ hàng trống. Vui lòng thêm sản phẩm.",
      });
      return;
    }

    if (cart.some((item) => !item.orderedWeight || item.orderedWeight <= 0)) {
      setMessage({
        type: "error",
        text: "Vui lòng nhập trọng lượng hợp lệ cho mỗi sản phẩm.",
      });
      return;
    }

    const orderRequest = {
      paymentMethod,
      items: cart.map((item) => ({
        productId: item.productId,
        orderedWeight: item.orderedWeight,
        pricePerUnit: item.pricePerUnit,
      })),
    };

    try {
      setIsSubmitting(true);
      const apiResponse = await createOrder(orderRequest);
      setMessage({
        type: "success",
        text: apiResponse.message || "Đơn hàng đã được tạo thành công.",
      });
      setCart([]);
    } catch (error) {
      const errorMessage =
        error?.response?.data?.message ||
        error.message ||
        "Không thể tạo đơn hàng.";
      setMessage({ type: "error", text: errorMessage });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-semibold text-slate-900">
        Seafood Ecommerce
      </h1>
      <p className="mt-4 text-slate-600">
        Chọn sản phẩm, thêm vào giỏ và hoàn tất đặt hàng.
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

      <section className="mt-8 grid gap-6 lg:grid-cols-[2fr_1fr]">
        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <h2 className="text-xl font-semibold text-slate-900">
            Danh sách sản phẩm
          </h2>
          <div className="mt-6 space-y-4">
            {isLoadingProducts && (
              <p className="text-slate-600">Đang tải danh sách sản phẩm...</p>
            )}

            {!isLoadingProducts && products.length === 0 && (
              <p className="text-slate-600">Chưa có sản phẩm nào.</p>
            )}

            {products.map((product) => (
              <div
                key={product.id}
                className="flex flex-col gap-4 rounded-2xl border border-slate-200 p-4 sm:flex-row sm:items-center sm:justify-between"
              >
                <div>
                  <h3 className="text-lg font-semibold text-slate-900">
                    {product.name}
                  </h3>
                  <p className="text-sm text-slate-600">
                    Đơn vị: {product.sellingUnit}
                  </p>
                  <p className="mt-1 text-slate-700">
                    Giá: {formatCurrency(product.basePrice)}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => handleAddToCart(product)}
                  className="rounded-full bg-sky-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-sky-700"
                >
                  Thêm vào giỏ
                </button>
              </div>
            ))}
          </div>
        </div>

        <div className="rounded-3xl bg-white p-6 shadow-sm">
          <h2 className="text-xl font-semibold text-slate-900">Giỏ hàng</h2>
          {cart.length === 0 ? (
            <p className="mt-4 text-slate-600">
              Giỏ hàng hiện chưa có sản phẩm.
            </p>
          ) : (
            <div className="mt-4 space-y-4">
              {cart.map((item) => (
                <div
                  key={item.productId}
                  className="rounded-2xl border border-slate-200 p-4"
                >
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <h3 className="font-semibold text-slate-900">
                        {item.name}
                      </h3>
                      <p className="text-sm text-slate-600">
                        Giá mỗi{" "}
                        {item.pricePerUnit > 0
                          ? formatCurrency(item.pricePerUnit)
                          : "---"}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleRemoveItem(item.productId)}
                      className="text-sm font-semibold text-rose-600 hover:text-rose-800"
                    >
                      Xóa
                    </button>
                  </div>

                  <div className="mt-4 grid gap-4 sm:grid-cols-[1fr_1fr]">
                    <label className="block text-sm text-slate-700">
                      Trọng lượng (KG)
                      <input
                        type="number"
                        min="0.1"
                        step="0.1"
                        value={item.orderedWeight}
                        onChange={(event) =>
                          handleWeightChange(item.productId, event.target.value)
                        }
                        className="mt-2 w-full rounded-xl border border-slate-300 px-4 py-2 text-slate-900 outline-none focus:border-sky-500"
                      />
                    </label>
                    <div className="rounded-2xl bg-slate-50 p-4 text-slate-700">
                      <p className="text-sm">Tạm tính</p>
                      <p className="mt-2 text-lg font-semibold text-slate-900">
                        {formatCurrency(item.subtotal)} 
                      </p>
                    </div>
                  </div>
                </div>
              ))}

              <div className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
                <p className="text-sm text-slate-600">
                  Phương thức thanh toán
                </p>
                <select
                  value={paymentMethod}
                  onChange={(event) => setPaymentMethod(event.target.value)}
                  className="mt-2 w-full rounded-xl border border-slate-300 bg-white px-4 py-2 text-slate-900 outline-none focus:border-sky-500"
                >
                  <option value="COD">COD</option>
                  <option value="VIETQR">VIETQR</option>
                </select>
              </div>

              <div className="rounded-3xl border border-slate-200 bg-white p-4">
                <p className="text-sm text-slate-600">Tổng đơn hàng</p>
                <p className="mt-2 text-2xl font-semibold text-slate-900">
                  {formatCurrency(cartTotal)}
                </p>
                <button
                  type="button"
                  onClick={handleCheckout}
                  disabled={isSubmitting}
                  className="mt-4 w-full rounded-full bg-sky-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-sky-700 disabled:cursor-not-allowed disabled:bg-slate-400"
                >
                  {isSubmitting ? "Đang xử lý..." : "Đặt hàng ngay"}
                </button>
              </div>
            </div>
          )}
        </div>
      </section>
    </main>
  );
}

export default Home;
