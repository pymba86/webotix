import {del, get, put} from "../common/fetch";
import {Job, ScriptJob} from "./types";

class JobService {
    async fetchJobs(): Promise<Job[]> {
        return await get("jobs");
    }

    async submitScriptJob(job: ScriptJob): Promise<Response> {
        return await put("scriptjobs/" + job.id, JSON.stringify({
            id: job.id,
            name: job.name,
            ticker: job.ticker,
            script: job.script
        }));
    }

    async submitJob(job: Job): Promise<Response> {
        return await put("jobs/" + job.id, JSON.stringify(job));
    }

    async deleteJob(id: string): Promise<Response> {
        return await del("jobs/" + id);
    }

    async fetchJob(id: string): Promise<Response> {
        return await get("jobs/" + id);
    }
}

export default new JobService();