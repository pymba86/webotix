import {get} from "../common/fetch";

class JobService {
    async fetchJobs(): Promise<Response> {
        return await get("jobs")
    }
}

export default new JobService();