import {SupportMetadata} from "../../modules/support";

export const SET_META = "support.SET_META"

export interface SetMetaAction {
    type: typeof SET_META;
    payload: {
        metadata: SupportMetadata
    };
}
export type SupportActionTypes = SetMetaAction;
