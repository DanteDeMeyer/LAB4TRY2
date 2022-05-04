docker build -t node .
start cmd /k "docker run -it --rm --name ns node java -jar project.jar nameserver 5555 230.0.0.0"
pause
start cmd /k "docker run -it --rm --name node-1 node java -jar project.jar node 5555 node-1 230.0.0.0"
pause
start cmd /k "docker run -it --rm --name node-2 node java -jar project.jar node 5555 node-2 230.0.0.0"
pause
start cmd /k "docker run -it --rm --name node-3 node java -jar project.jar node 5555 node-3 230.0.0.0"