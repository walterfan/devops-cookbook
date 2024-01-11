# plantuml
- refer [plantuml.com](https://plantuml.com/) for details manual doc.
- Visual Studio Code is good IDE to write Markdown document, that is "What You See Is What You Get"
    - install ```PlantUML extension```
    - set configs, ```plantuml.render``` and ```plantuml.server```. please use local plantuml server ```http://{host-ip}:8888/uml/```

    - write Markdown doc in Visual Studio Code, that is "What You See Is What You Get"

- After done, copy plantuml code into online http://{host-ip}:8888/uml/ to generate desgin digram's link. add the link in markdown doc

- It's better to make plantuml code as hide-text to make document easy readable.
    ```
    <details>
    <summary>Click to view details of plantuml code</summary>

        plantuml code blocks

    </details>
    ```
- It has one limitation, github prevent internal domain name and ip, but don't prevent public domain. as the page before,it show image normal, it link ```plantuml.com```. but if link with ```{host-ip}```, it show icon, refer to "[appshare module user guide](../../mediaengine/appshare/docs/appshare-user-guide.md)" as example. please use internal ```{host-ip}``` though it is inconvenient.