import styled from "styled-components";
import * as React from "react";

const LayoutBox = styled.div`
  height: 100%;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.2);
`;

export interface Props {
    isMobile: boolean;
}


export default class Framework extends React.Component<Props> {

    render() {
        return (
            <>

            </>
        )
    }
}
