import React, {ErrorInfo} from "react"

export interface ErrorWrapperProps {
    message: string;
    children: React.ReactNode;
}

export interface ErrorBoundaryProps {
    wrapper?: React.ComponentType<ErrorWrapperProps>;
}

export interface ErrorBoundaryState {
    error: Error | null | undefined;
    errorInfo: ErrorInfo | null | undefined;
}

export class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {

    constructor(props: ErrorBoundaryProps) {
        super(props);
        this.state = {error: null, errorInfo: null}
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        this.setState({
            error: error,
            errorInfo: errorInfo
        })
    }

    render() {
        if (this.state.errorInfo) {
            const Wrapper = this.props.wrapper
                ? this.props.wrapper
                : ({message, children}: ErrorWrapperProps) => (
                    <div>
                        <h2>{message}</h2>
                        {children}
                    </div>
                );

            return (
                <Wrapper message="Something went wrong">
                    <details style={{whiteSpace: "pre-wrap"}}>
                        {this.state.error && this.state.error.toString()}
                        <br/>
                        {this.state.errorInfo.componentStack}
                    </details>
                </Wrapper>
            )
        }
        return this.props.children
    }
}