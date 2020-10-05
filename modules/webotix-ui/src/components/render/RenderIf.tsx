import React from 'react';

export interface RenderIfProps {
    condition: boolean;
}

export class RenderIf extends React.Component<RenderIfProps> {

    shouldComponentUpdate(nextProps: Readonly<RenderIfProps>): boolean {
        return nextProps.condition
    }

    render() {
        return this.props.children;
    }
}