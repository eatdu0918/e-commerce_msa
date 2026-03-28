/** API catalog fields: canonical English + optional Korean */

export function preferKoreanUi(lang: string | undefined): boolean {
    return !lang?.toLowerCase().startsWith('en');
}

export function catalogProductName(
    p: { name: string; nameKo?: string | null },
    lang: string | undefined
): string {
    if (preferKoreanUi(lang) && p.nameKo) {
        return p.nameKo;
    }
    return p.name;
}

export function catalogProductDescription(
    p: { description: string; descriptionKo?: string | null },
    lang: string | undefined
): string {
    if (preferKoreanUi(lang) && p.descriptionKo) {
        return p.descriptionKo;
    }
    return p.description ?? '';
}

export function catalogCategoryName(
    c: { name: string; nameKo?: string | null },
    lang: string | undefined
): string {
    if (preferKoreanUi(lang) && c.nameKo) {
        return c.nameKo;
    }
    return c.name;
}
