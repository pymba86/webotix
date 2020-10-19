import {del, get, put} from "../common/fetch";
import {Script} from "./types";


class ScriptService {
    async fetchScripts(): Promise<Script[]> {
        return await get("scripts");
    }

    async fetchScript(id: string): Promise<Script> {
        return await get("scripts/" + id);
    }

    async saveScript(script: Script): Promise<Response> {
        return await put("scripts/" + script.id, JSON.stringify(script));
    }

    async deleteScript(id: string): Promise<Response> {
        return await del("scripts/" + id);
    }
}

export default new ScriptService();