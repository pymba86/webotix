import React, {RefObject} from 'react'

const NOOP = () => void 0;

export type OutsideClickProps<T extends HTMLElement> = {
    containerRef: RefObject<T>;
    onInsideClick?: (event: Event) => void
    onOutsideClick?: (event: Event) => void
}

export const useOutsideClick = <T extends HTMLElement>({
                                                           containerRef,
                                                           onInsideClick = NOOP,
                                                           onOutsideClick = NOOP,
                                                       }: OutsideClickProps<T>) => {
    React.useEffect(() => {
        const handleDocumentClick = (event: Event) => {
            if (containerRef && containerRef.current) {
                const isOutsideClick = !containerRef.current.contains(event.target as Node);
                isOutsideClick ? onOutsideClick(event) : onInsideClick(event);
            }
        }

        document.addEventListener('mousedown', handleDocumentClick, true)

        return () => {
            document.removeEventListener('mousedown', handleDocumentClick, true)
        }
    }, [containerRef, onInsideClick, onOutsideClick])
}