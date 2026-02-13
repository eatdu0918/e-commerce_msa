declare module '@portone/browser-sdk/v2' {
    interface PaymentRequest {
        storeId: string;
        channelKey: string;
        paymentId: string;
        orderName: string;
        totalAmount: number;
        currency: 'CURRENCY_KRW';
        payMethod: 'CARD' | 'VIRTUAL_ACCOUNT' | 'TRANSFER' | 'MOBILE' | 'EASY_PAY';
        customer?: {
            customerId?: string;
            fullName?: string;
            phoneNumber?: string;
            email?: string;
            address?: {
                country?: string;
                addressLine1?: string;
                addressLine2?: string;
                city?: string;
                province?: string;
                zipcode?: string;
            };
            zipcode?: string;
        };
        windowType?: {
            pc?: 'IFRAME' | 'POPUP' | 'NEW';
            mobile?: 'IFRAME' | 'POPUP' | 'NEW';
        };
        redirectUrl?: string;
    }

    interface PaymentResponse {
        code?: string;
        message?: string;
        paymentId: string;
        transactionType: string;
        txId: string;
        totalAmount: number;
    }

    export function requestPayment(request: PaymentRequest): Promise<PaymentResponse>;
}
