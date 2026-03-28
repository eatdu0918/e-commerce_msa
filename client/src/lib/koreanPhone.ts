/** 숫자만 추출 (하이픈/공백 등 제거) */
export function digitsOnlyPhone(phone: string): string {
    return phone.replace(/\D/g, '');
}

/**
 * 한국 휴대폰 번호 검증 (하이픈 있음·없음 모두 허용)
 * 예: 010-1234-5678, 01012345678, 011-123-4567
 */
export function isValidKoreanMobile(phone: string): boolean {
    const d = digitsOnlyPhone(phone);
    return /^01[016789]\d{7,8}$/.test(d);
}
