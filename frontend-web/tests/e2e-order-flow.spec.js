import { expect, test } from '@playwright/test';

test('client places an order and warehouse processes it', async ({ page }) => {
  test.setTimeout(60_000);
  const productName = 'C\u00e1 T\u1ea7m Sapa nguy\u00ean con';

  await page.goto('http://localhost:5173');

  await expect(page.getByText(productName)).toBeVisible();

  await page
    .locator('div')
    .filter({ hasText: productName })
    .getByRole('button')
    .first()
    .click();

  const createOrderResponse = page.waitForResponse((response) =>
    response.url() === 'http://localhost:8080/api/v1/orders' &&
    response.request().method() === 'POST' &&
    response.status() === 201,
  );

  await page.getByRole('button', { name: /\u0111\u1eb7t h\u00e0ng ngay/i }).click();

  const createOrderJson = await (await createOrderResponse).json();
  const orderCode = createOrderJson.data.orderCode;

  await expect(page.getByText(/th\u00e0nh c\u00f4ng/i)).toBeVisible();

  await page.goto('http://localhost:5173/admin');

  const pendingOrder = page.locator('article').filter({
    hasText: orderCode,
  });
  await expect(pendingOrder).toBeVisible();
  await expect(pendingOrder.getByText(productName)).toBeVisible();
  await expect(pendingOrder.getByText(/du kien 0\.5 KG/i)).toBeVisible();

  await pendingOrder.getByRole('button', { name: /xu ly/i }).click();

  await page.getByRole('spinbutton').fill('1.5');

  await Promise.all([
    page.waitForResponse((response) =>
      response.url() ===
        `http://localhost:8080/api/v1/orders/${createOrderJson.data.id}/actual-weights` &&
      response.request().method() === 'PUT' &&
      response.status() === 200,
    ),
    page
      .getByRole('button', { name: /xac nhan can va tru kho/i })
      .click({ force: true }),
  ]);

  await expect(pendingOrder).toHaveCount(0);
});
