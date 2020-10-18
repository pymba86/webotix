import React, {useCallback} from "react";
import {Section} from "../elements/section";
import {useVisibility} from "../components/visibility/Visibility";
import {useHistory} from "react-router";
import {RenderIf} from "../components/render/RenderIf";
import {SectionLink} from "../elements/section/SectionLink";
import {Icon} from "../elements/icon";
import {ScriptEditor} from "../components/script";

export const ScriptContainer: React.FC = () => {

    const visible = useVisibility();
    const history = useHistory();

    const handleAddCoin = useCallback(
        () => history.push('/addCoin'), [history]);

    return (
        <RenderIf condition={visible}>
            <Section id={"scripts"}
                     heading={"Scripts"}
                     nopadding={true}
                     buttons={() => (
                         <SectionLink onClick={handleAddCoin}>
                             <Icon type="plus"/>
                         </SectionLink>
                     )}>
                <ScriptEditor/>
            </Section>
        </RenderIf>
    )
};