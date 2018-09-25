# Tomcat Clustering in OpenShift

Tomcat-in-the-cloud is the current name of the project that seeks to port Tomcat clustering into cloud services such as Kubernetes and OpenShift.

## Try it out with Minishift
### Requirements
* [Docker](https://docs.docker.com/v17.12/install/)
* [Kubernetes CLI](https://kubernetes.io/docs/tasks/tools/install-kubectl/) (to orchestrate from the command line)

### Setting up the environment

Minishift is a light-weight OpenShift platform that is easy to install and quickly up and running. This platform is perfect for testing out new features like Tomcat-in-the-Cloud. To download and install Minishift, refer to the [official documentation](https://docs.okd.io/latest/minishift/getting-started/installing.html).

Once Minishift is installed, proceed with the following instructions :

1. Start Minishift
```sh
$ minishift start
```

2. Set up the docker environment
```sh
$ eval $(minishift docker-env)
```

3. Login with a user/password of your choice. We'll use admin/password
```sh
$ oc login -u admin -p password
```

4. Create a new project. Again, we will call it tomcat-in-the-cloud but feel free to use the one you'd like
```sh
$ oc new-project tomcat-in-the-cloud
```

5. Using Docker, log into the running Openshift docker registry
```sh
$ docker login -u $(oc whoami) -p $(oc whoami -t) $(minishift openshift registry)
```

6. If not already done, clone the repository and change directory to get to its root
```sh
$ git clone https://github.com/web-servers/tomcat-in-the-cloud.git
$ cd tomcat-in-the-cloud
```

7. Build the docker image providing the war file of the application you would like to deploy (relative path). You also have to specify the registry_id which is the name of your Openshift project
```sh
$ docker build --build-arg war=[war_file] --build-arg registry_id=$(oc project -q) . -t $(minishift openshift registry)/$(oc project -q)/image
```
The name of the project can be retreived using ```$(oc project -q)```.

8. Push the resulting docker image onto the docker registry
```sh
$ docker push $(minishift openshift registry)/$(oc project -q)/image
```

### Deploying in the cloud
Once built and pushed on the docker registry, we need to run and expose the docker image.

1. Allow Openshift pods to see their peers (a must for session replication to work)
```sh
$ oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default -n $(oc project -q)
```

2. Run the docker image into one container
```sh
$ kubectl run $(oc project -q) --image=$(minishift openshift registry)/$(oc project -q)/image:latest --port 8080
```

3. Expose the deployment on port 80 for it to be accessible
```sh
$ kubectl expose deployment $(oc project -q) --type=LoadBalancer --port 80 --target-port 8080
```

Your clustered application should now be up and running, you can connect to Openshift GUI using ```$ minishift console```. When done simply log in and manage your application!

## Credits

Classes in the package `org.example.tomcat.cloud.stream` are taken from the
[JGroups-Kubernetes](https://github.com/jgroups-extras/jgroups-kubernetes/)
project. This project also served as inspiration for our implementation.
