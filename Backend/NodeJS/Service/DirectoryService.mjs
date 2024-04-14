const GetDirectory = async (db, paths) => {
    try {
        // Access the "directories" collection
        const col = db.collection("directories");

        // Find the root directory by its ID
        let root = null;
        root = await col.findOne({"_id": paths[0]});

        if (root != null) {

            let children = root.children;

            //Start iterating from 1 as we already used 0
            for (let i = 1; i < paths.length; i++) {
                let currentChild = null;
                currentChild = children[paths[i]];

                if (currentChild != null) {
                    root = currentChild;
                }
            }
        }

        // Log the root directory
        console.log(root);

        // Return the root directory
        return root;
    } catch (error) {
        // Handle errors
        console.error("Error in GetDirectory:", error);
        throw error; // Re-throw the error for handling at a higher level
    }
};

export default GetDirectory;
