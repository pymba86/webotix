import * as React from "react";
import styled from "styled-components";
import {color} from "styled-system";

const SectionBox = styled.section`
  ${color};
  margin: 0;
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.2);
`;

const SectionInner = styled.section`
  background-color: ${props => props.theme.colors.backgrounds[1]};
  flex: 1;
  position: relative;
`;

interface Props {
    children?: React.ReactNode;
}

export default class Section extends React.Component {


    render() {

        return (
            <SectionBox>
                <SectionInner>
                    {this.props.children}
                </SectionInner>
            </SectionBox>
        )
    }
}
