import dayjs from 'dayjs';
import 'dayjs/locale/ko';

dayjs.locale('ko');

export const formatDate = (date: string | Date | undefined, format = 'YYYY년 M월 D일') => {
    if (!date) return '';
    return dayjs(date).format(format);
};

export const formatDateTime = (date: string | Date | undefined, format = 'YYYY.MM.DD HH:mm') => {
    if (!date) return '';
    return dayjs(date).format(format);
};

export const formatRelative = (date: string | Date | undefined) => {
    if (!date) return '';
    const now = dayjs();
    const target = dayjs(date);
    const diff = now.diff(target, 'day');

    if (diff === 0) return '오늘';
    if (diff === 1) return '어제';
    if (diff < 7) return `${diff}일 전`;
    return target.format('YYYY.MM.DD');
};
