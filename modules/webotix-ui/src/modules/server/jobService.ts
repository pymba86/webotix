import {get, put} from "../common/fetch";
import {Job, ScriptJob} from "./types";

class JobService {
    async fetchJobs(): Promise<Job[]> {
        return await get("jobs");
    }

    async submitScriptJob(job: ScriptJob): Promise<Response> {
        return (await put("scriptjobs/" + job.id, JSON.stringify(job)));
    }
}

export default new JobService();