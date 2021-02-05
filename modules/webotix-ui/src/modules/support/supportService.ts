import {get} from "../common/fetch";
import {SupportMetadata} from "./types";

class SupportService {
    async fetchMetadata():  Promise<SupportMetadata> {
        return await get("support/meta")
    }
}

export default new SupportService();
