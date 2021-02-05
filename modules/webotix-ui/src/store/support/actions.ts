import {SET_META, SupportActionTypes} from "./types";
import {SupportMetadata} from "../../modules/support";

export function setMetadata(metadata: SupportMetadata): SupportActionTypes {
    return {
        type: SET_META,
        payload: {metadata}
    }
}
