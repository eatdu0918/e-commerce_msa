import DaumPostcode from 'react-daum-postcode';
import { X } from 'lucide-react';
import { useScrollLock } from '../../hooks/useScrollLock';

interface AddressSearchModalProps {
    isOpen: boolean;
    onClose: () => void;
    onComplete: (address: string, zonecode: string) => void;
}

export default function AddressSearchModal({ isOpen, onClose, onComplete }: AddressSearchModalProps) {
    useScrollLock(isOpen);
    if (!isOpen) return null;

    const handleComplete = (data: any) => {
        let fullAddress = data.address;
        let extraAddress = '';

        if (data.addressType === 'R') {
            if (data.bname !== '') {
                extraAddress += data.bname;
            }
            if (data.buildingName !== '') {
                extraAddress += (extraAddress !== '' ? `, ${data.buildingName}` : data.buildingName);
            }
            fullAddress += (extraAddress !== '' ? ` (${extraAddress})` : '');
        }

        onComplete(fullAddress, data.zonecode);
        onClose();
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
            <div className="bg-white rounded-2xl w-full max-w-md overflow-hidden relative shadow-2xl">
                <div className="flex justify-between items-center p-4 border-b border-gray-100">
                    <h3 className="font-bold text-lg">주소 검색</h3>
                    <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
                        <X size={20} />
                    </button>
                </div>
                <div className="h-[500px]">
                    <DaumPostcode
                        onComplete={handleComplete}
                        style={{ height: '100%' }}
                        className="no-scrollbar"
                    />
                </div>
            </div>
        </div>
    );
}
