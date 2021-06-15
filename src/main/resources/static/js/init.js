;(function(window, Utils) {
    'use strict';

    var existingServerListApis = [
        "https://raw.githubusercontent.com/ainilili/ratel/master/serverlist.json",
    ];
    var existingServerList = [
        "47.103.16.48:1024:Demo[V1.2.7]",
        "121.5.140.133:1024:Nico[v1.2.7]"
    ];

    function Server(s) {
        if (!Server.pattern.test(s)) {
            throw new Error("Illegal server address. Server address schema like: ip:port:name[version].");
        }
        var arr = Server.pattern.exec(s);
        this.host = arr[1];
        this.port = parseInt(arr[2]) + 1;
        if (arr[3]) this.name = arr[3];
        if (arr[4]) this.version = parseInt(arr[4].replace(/\./g, ""));
    }

    Server.pattern = /([\w\.]+):(\d+)(?::(\w+)\[v(1\.\d\.\d)\])?/i;
    Server.requiredMinVersion = "v1.2.7";

    Server.prototype.compareVersion = function(otherVersion) {
        if (otherVersion.startsWith("v") || otherVersion.startsWith("V")) {
            otherVersion = otherVersion.substr(1);
        }

        return this.version - parseInt(otherVersion.replace(/\./g, ""));
    };

    Server.prototype.toString = function() {
        var s = this.host + ":" + this.port;
        if (this.name) s += ":" + this.name;
        if (this.version) s += "[v" + this.version + "]";
        return s;
    };

    // ---------------------------------------------------------------------------------------------
    var defaultLoadTimeout = 3000;

    function loadServerList() {
        existingServerListApis.forEach((api, i) => {
            Utils.timeout(() => {
                return fetch(api)
                        .then(response => response.json())
                        .catch(() => []);
            }, defaultLoadTimeout)
            .then(data => showServerList(existingServerList = data.length > 0 ? data : existingServerList))
            .catch(showServerList);
        });
    }

    function showServerList() {
        var contentDiv = document.querySelector("#content");
        existingServerList.forEach((server, i) => {
            contentDiv.innerHTML += (i + 1) + ". &nbsp;" + server + "</br>";
        });

        var input = document.querySelector("#input");
        input.addEventListener("keypress", selectServer, false);
        input.focus();
    }

    function selectServer(e) {
        if (e.keyCode != 13) {
            return;
        }

        var contentEl = document.querySelector("#content");
        var input = document.querySelector("#input");
        var s = input.value.trim();
        input.value = "";
        if (s == "" || s === null || typeof s === "undefined") {
            contentEl.innerHTML += "Invalid input, please input again.</br>";
            return;
        }

        if (s.length <= Math.ceil(existingServerList.length / 10)) {
            var server;
            try {
                var i = parseInt(s);
                if (!Number.isNaN(i)) {
                    if (i > 0 && i <= existingServerList.length) {
                        try {
                            server = new Server(existingServerList[i - 1]);
                            if (server.compareVersion(Server.requiredMinVersion) < 0) {
                                contentEl.innerHTML += "Server version must >= v1.2.7. please choose another server.</br>";
                                return;
                            }
                        } catch(e) {
                            contentEl.innerHTML += "Illegal server address. please choose another server.</br>";
                            return;
                        }
                    } else {
                        contentEl.innerHTML += "Invalid option " + i + ", please input again.</br>";
                        return;
                    }
                }
            } catch (e) {
                console.error("Program error, abnormal exit.\n", e);
                return;
            }
        } else {
            try {
                server = new Server(s);
            } catch(e) {
                contentEl.innerHTML += "Invalid server address : " + s + ", please input again.</br>";
                return;
            }
        }

        start(server.host, server.port)
            .then(() => input.removeEventListener("keypress", selectServer, false))
            .catch(e => {
                console.error(e);
                contentEl.innerHTML += "Connect server [" + server.toString() + "] fail, please choose another server.</br>";
            });
    }

    function start(host, port) {
        if (typeof host === "undefined") {
            host = "127.0.0.1";
        }
        if (typeof port === "undefined") {
            port = 1025;
        }

        document.querySelector("#content").innerHTML += "Connect to ws://" + host + ":" + port + "/ratel .</br></br>";

        window.wsClient = new WsClient("ws://" + host + ":" + port + "/ratel");
        return window.wsClient.init();
    }

    function incrPv() {
        var host = window.location.host;
        fetch("http://" + host + "/js-client/pv/i", {method: "POST"});
    }

    window.onload = function() {
        incrPv();
        defaultSite.render();
        loadServerList();
    };
} (this, this.Utils));