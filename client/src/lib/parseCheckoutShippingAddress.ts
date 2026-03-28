/**
 * CheckoutPage에서 조합한 배송지 문자열 "(우편번호) 도로명 상세" 파싱
 */
export function parseCheckoutShippingAddress(
    shipping: string,
): { zonecode: string; roadAddress: string; detailAddress: string } | null {
    const trimmed = shipping.trim();
    const m = trimmed.match(/^\((\d{4,5})\)\s+([\s\S]*)$/);
    if (!m) return null;
    const rest = m[2].trim();
    return { zonecode: m[1], roadAddress: rest, detailAddress: '' };
}
