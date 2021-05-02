import React, {useState} from "react";
import {Section} from "../elements/section";
import {useVisibility} from "../components/visibility/Visibility";
import {useHistory} from "react-router";
import {RenderIf} from "../components/render/RenderIf";
import {SectionLink} from "../elements/section/SectionLink";
import {Icon} from "../elements/icon";
import {ScriptManager} from "../components/script";
import {AddScriptContainer} from "./AddScriptContainer";
import {RootState} from "../store/reducers";
import {Script} from "../modules/script";
import * as scriptActions from "../store/scripts/actions";
import {connect, ConnectedProps} from "react-redux";
import {ScriptControlContainer} from "./ScriptControlContainer";


const mapState = (state: RootState) => ({
    selectedScript: state.scripts.selectedScript
});

const mapDispatch = {
    addScript: (script: Script) => scriptActions.addScript(script),
    selectScript: (script: Script) => scriptActions.selectScript(script)
};

const connector = connect(mapState, mapDispatch);

type StateProps = ConnectedProps<typeof connector>;

const ScriptWrapper: React.FC<StateProps> = ({
                                                 addScript,
                                                 selectScript
                                             }) => {

    const visible = useVisibility();
    const history = useHistory();

    const [visibleAddScript, setVisibleAddScript] = useState<boolean>(false);

    const [visibleScriptControl, setVisibleScriptControl] = useState<boolean>(false);

    return (
        <RenderIf condition={visible}>
            <Section id={"scripts"}
                     heading={"Scripts"}
                     nopadding={true}
                     buttons={() => (
                         <SectionLink onClick={() => setVisibleAddScript(true)}>
                             <Icon type="plus"/>
                         </SectionLink>
                     )}>
                <ScriptManager onControl={() => setVisibleScriptControl(true)}/>

                {visibleAddScript && (
                    <AddScriptContainer
                        visible={visibleAddScript}
                        onClose={() => setVisibleAddScript(false)}
                        addScript={addScript}/>
                )}

                {visibleScriptControl && (
                    <ScriptControlContainer
                        visible={visibleScriptControl}
                        selectScript={selectScript}
                        onClose={() => setVisibleScriptControl(false)}/>
                )}

            </Section>
        </RenderIf>
    )
};

export const ScriptContainer = connector(ScriptWrapper);