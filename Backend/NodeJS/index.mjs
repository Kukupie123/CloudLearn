import {MongoClient, ServerApiVersion} from "mongodb";
import GetDirectory from "./Service/DirectoryService.mjs";

const mongoPassword = process.env.MONGODB_PASS;
const mongoUser = process.env.MONGODB_USER;
const uri = 'mongodb+srv://' + mongoUser + ':' + mongoPassword + '@cluster0.l7o1grf.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0';
const client = new MongoClient(uri, {
    serverApi: {
        version: ServerApiVersion.v1, strict: true, deprecationErrors: true,
    }
});

export const handler = async (event) => {
    try {
        await client.connect();
        const db = client.db("OS_Learner_GLOBAL");
        const body = event.body;
        if (body == null) {
            return resp(401, JSON.stringify({
                "message": "Body is missing", "data": null
            }));
        }

        const jsonBody = JSON.parse(body);
        const action = jsonBody.action;

        if (action != null) {
            switch (action) {
                case "Directory.GetDirectory":
                    const paths = jsonBody.path;
                    if (paths == null) {
                        return resp(401, mapToString({
                            "message": "path field is missing in body"
                        }))
                    }
                    const dir = await GetDirectory(db, paths);
                    let statusCode = 404;
                    let respBody = JSON.stringify({
                        "message": "Record Not Found", "data": null
                    });
                    if (dir != null) {
                        statusCode = 200
                        respBody = JSON.stringify({
                            "message": "Found Record", "data": dir
                        });
                    }
                    return resp(statusCode, respBody);

                default:
                    return resp(401, JSON.stringify({
                        "message": `Action : ${action} not configured.`
                    }))
            }
        } else {
            return resp(401, "Bad Request, Action is missing");
        }
    } catch (error) {
        console.error('Error:', error);
        return resp(500, JSON.stringify({message: error.message}));
    } finally {
        await client.close();
    }
};

const mapToString = (data) => {
    return JSON.stringify(data)
}
const resp = (statusCode, body, header) => {
    return {
        statusCode: statusCode, body: body, header: header,
    };
};
