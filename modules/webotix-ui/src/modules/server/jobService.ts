import {get} from "../common/fetch";
import {Job} from "./types";

class JobService {
    async fetchJobs(): Promise<Job[]> {
        return await get("jobs")
    }
}

export default new JobService();