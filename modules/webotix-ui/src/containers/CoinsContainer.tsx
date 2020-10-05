import React, {useCallback} from "react";
import {useVisibility} from "../components/visibility/Visibility";
import {RenderIf} from "../components/render/RenderIf";
import {Section} from "../elements/section";
import {useHistory} from "react-router-dom"
import {Icon} from "../elements/icon";
import {SectionLink} from "../elements/section/SectionLink";

export const CoinsContainer: React.FC = () => {

    const visible = useVisibility();
    const history = useHistory();

    const handleAddCoin = useCallback(
        () => history.push('/addCoin'), [history]);

    return (
        <RenderIf condition={visible}>
            <Section id={"coins"}
                     heading={"Coins"}
                     nopadding={true}
                     buttons={() => (
                         <SectionLink onClick={handleAddCoin}>
                             <Icon type="plus"/>
                         </SectionLink>
                     )}>
                1234
            </Section>
        </RenderIf>
    )
}