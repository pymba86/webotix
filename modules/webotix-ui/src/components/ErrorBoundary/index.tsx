import * as React from "react";

interface Props {
    wrapper?: React.ReactNode;
}

interface State {
    error: Error | null;
    errorInfo: React.ErrorInfo | null;
}

export default class ErrorBoundary
    extends React.Component<Props, State> {

    state: State = {
        error: null,
        errorInfo: null
    };

    componentDidCatch(error: Error, errorInfo: React.ErrorInfo): void {
        this.setState({
            error: error,
            errorInfo: errorInfo
        })
    }

    public render() {
        if (this.state.errorInfo) {
            return (
                <div>
                    <h2>Something went wrong</h2>
                    <details style={{ whiteSpace: "pre-wrap" }}>
                        {this.state.error && this.state.error.toString()}
                        <br />
                        {this.state.errorInfo.componentStack}
                    </details>
                </div>
            )
        }
        return this.props.children;
    }

}
