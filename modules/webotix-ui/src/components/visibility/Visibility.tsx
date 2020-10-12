import {useEffect, useState} from "react"

const hasDocument = typeof document !== 'undefined';

export type VendorEventState = 'visibilityState';

export type VendorEventHidden = 'hidden';

export type VendorEvent = {
    hidden: VendorEventHidden,
    event: string,
    state: VendorEventState,
}

const vendorEvents: VendorEvent[] = [
    {
        hidden: 'hidden',
        event: 'visibilitychange',
        state: 'visibilityState',
    },
];

export const isSupported = hasDocument && Boolean(document.addEventListener);

export const visibility: VendorEvent | null = (() => {
    if (!isSupported) {
        return null;
    }
    for (let i = 0; i < vendorEvents.length; i++) {
        const event = vendorEvents[i];
        if (event.hidden in document) {
            return event;
        }
    }
    // otherwise it's not supported
    return null;
})();

export type VendorEventHandler = () => [boolean, "hidden" | "visible"];

export const getHandlerArgs: VendorEventHandler = () => {
    if (!visibility) return [true, "hidden"];
    const {hidden, state} = visibility;
    return [!document[hidden], document[state]];
};

const isSupportedLocal = isSupported && visibility;

export const useVisibility: () => boolean = () => {
    const [initiallyVisible] = getHandlerArgs();

    const [isVisible, setIsVisible] = useState(initiallyVisible);

    useEffect(() => {
        if (isSupportedLocal) {
            const handler = () => {
                const [currentlyVisible] = getHandlerArgs();

                setIsVisible(currentlyVisible);
            };

            if (visibility) {
                document.addEventListener(visibility.event, handler);
            }

            return () => {
                if (visibility) {
                    document.removeEventListener(visibility.event, handler);
                }
            };
        }
    }, []);

    return isVisible;
};