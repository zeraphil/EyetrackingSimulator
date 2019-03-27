
# Eyetracking Simulator

Eyetracking simulator built for Android Oreo and test on Android Emulator and Google Pixel.

To run, open the project, let Gradle sync, and build the APK. 
All dependencies are built into Gradle.
unityapp module has a classpath dependency that is included in the repo, classes.jar

## Structure

The project is composed of two logical components: an eyetracking data consumer, and an eyetracking data producer. In reality, the structure is broken down further to appease modularity. 

The app module has the main activity and the visualization for the project. It additionally has a UI module described as follows.
- UI: custom View and class for drawing the "eye positions"

The unityapp module has the extension of the UnityPlayerActivity to replace the default UnityPlayer for an Android Unity project (Unity project not completed).
The Eyetracking module has the code shared between the two apps, and is comprised of several components.

- Simulator: Simulators implement an update method that generates a packet of eyetracking data in the schema provided.
- Service: Service implementations for both production and consumption of eyetracking dataa
- DataModel: data classes and database descriptions, including the schema for the database and the schema for the flatbuffer transport library
- Tasks : AsyncTasks, currently only a single task for database entry
- Tests: Instrumentation tests, mainly for the serialization processes

 Timestamp timestamp : long seconds, int nanoseconds;
 boolean id;
 float confidence;
 float normalizedPosX;
 float normalizedPosY;
 int pupilDiameter;

 an additional data memeber, uniqueID, was added for testing and database insertion as primary key.


#### Simulators
 Two simulators are provided: random, and eyeball, with random being used for testing, and eyeball being used for the app itself.
 Eyeball tries to simulate eye movement a bit closer than just pure random movement. It does this by approximating the chord length described by an saccade at each time step, assuming a fixed distance from "eyes" to "screen", using a target to simulate "where the eyes would be looking at".

#### Service
 Services are further broken down into two main parts: The messenger services and the database service. The Messenger services both inherit form EyetrackingSimulatorService, which provides the simulation logic and the incoming message handling logic. After this, two services were created: EyetrackingMessengerService, which does IPC to the activity through the Binder framework and parcel transport library, and EyetrackingFlatBufferService, which also uses the Binder framework but uses Flatbuffers as the serializer. The second service was created as an answer to the question of "what to use if using a wireless networking connection" rather than same device IPC. Flatbuffer serialization is so quick that the difference between it and Parcel is nearly neglibile.

 The other service, DatabaseRoomService, is an appropriately named service implementing Android's Room Sqlite wrapper for data persistence. This is done on a separate thread, with data dumps happening about once every second through the use of a LinkedBlockingQueue. 

#### DataModel
Data Model implements the EyetrackingData model, which itself has methods for parcel and flat buffer serialization, with the latter meant for more general purpose serialization and the first meant for absolute speed. The database implementation uses Room, which simplifies the creation of an sqlite db in Android. so, a wrapper for creating a database entitiy object is in the EyetrackingDatabaseEntity as a model.

- Internal to the data model is the flat buffer schema, which mirrors the schema above. Additional components for this are the compiled flat buffer components to support said schema. An example of the schema (schema.fbs) is inside the project source.

#### UI
A custom DrawView is used to represent the two eye positions, which transform the normalized positions into the view coordinates. Nothing special beyond that here.

####Tests 
A few instrumentation tests are provided
- A test for room persistence, or writing to the Room database, which also tests the eyetracking simulator
- A test for flat buffer serialization
- A test for bound service testing (incomplete)

## Considerations

- Initially, Parcel was selected as the serialization library of choice because it is unmatched on Android for speed and support. That said, it is not scalable to other transport libraries, so FlatBuffers was added as a sort of fallback library for more general purpose serialization without sacrificing speed (and for my own personal learning experience). Long term, however, Protocol Buffers might be a better choice. 

- No process matching handling was needed for this case, as the services are marked with exported:false, preventing other processes from binding to them on Android. If this was needed, additional logic would be needed in the register/unregister messages, mainly with a handshake and some process signing. 

- Data is logged in batches to Android's wrapped Sqlite library (Room). This would make post-hoc queries simple.

- A wireless connection between eyetracker and component A would then require additional implementation on a message queue. Depending on the type of wireless stack, the deserialization and packet structure would have to be benchmarked to avoid affecting latency. 