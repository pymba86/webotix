import {useEffect, useRef, DependencyList} from "react"

export function useInterval(callback: () => void, delay: number | null, dependencies: DependencyList = []) {

    const savedCallback = useRef<() => void>(callback);

    // Remember the latest callback.
    useEffect(() => {
        savedCallback.current = callback
        // eslint-disable-next-line
    }, [callback].concat(dependencies));

    // Set up the interval.

    useEffect(() => {
        function tick() {
            savedCallback.current()
        }

        if (delay !== null) {
            let id = setInterval(tick, delay);
            return () => clearInterval(id)
        }
        // eslint-disable-next-line
    }, [delay].concat(dependencies))
}
