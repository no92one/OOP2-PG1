class EditSite extends Component{

    events(){
        $('body').on('submit', '#logout', this.logout)
        $('body').on('submit', '#main', this.main)
        $('body').on('submit', '#editSite', this.editSite)
        $('body').on('submit', '#deleteSite', this.deleteSite)
        $('body').on('button', '#editSite', this.loadSite) // Från Linus?

    }

    async editSite(event){
         event.preventDefault()
         let result = await fetch(apiHost + '/site/edit', {
             method: 'put',
             headers: { 'Content-Type': 'application/json' },
             body: JSON.stringify({
                 "adminId": user.username,
                 "title": document.querySelector('#title').value,
                 "description": document.querySelector('#description').value,
                 "colorTheme": document.querySelector('#colorInputText').value,
                 "font": document.querySelector('#fontInput').value
             })
         })
         let data = await result.json()
         console.log(result, data)

         if(result.status === 200){ // goes to new site if login status is 200 (200 = successful login)
            alert("You Updated your Site!")
            location.hash = "editSite"
        }
        if(result.status === 400){ 
            alert("This Site Dosen't Exist!")
            location.hash = "editSite"
        }
        if(!result.status === 200){
            alert("Something went wrong!?")
        }

     }

     async deleteSite(event){
        event.preventDefault()
            let result = await fetch(apiHost + `/site/ ${sitetitle}`, {
            method: 'delete'
        })

        if(result.status === 200){ // goes to new site if login status is 200 (200 = successful login)
           alert("Deleted!")
           location.hash = "editSite"
       }
       if(result.status === 400){ 
           alert("Didn't find that site!")
           location.hash = "editSite"
       }
       if(!result.status === 200){
           alert("Something went wrong!?")
       }
     }

    siteTitle = []

    async load(){
        let result = await fetch(apiHost + '/site/get/' + user.username)    
        let data = await result.json()
        console.log(result,data);

        this.data = data

    }

    async loadSite(e){
        let result = await fetch(apiHost + '/site/get') 

    }

    async main(e){
        location.hash = "mainAdminPage"
    }

    async logout(event){
        event.preventDefault()
        let result = await fetch(apiHost + '/api/auth/signout', {
            method: 'delete',
        })
        let data = await result.json()
        console.log(result, data)
        if(result.status === 200){
           location.hash = "login"
        }
    }

    siteLinks(sites) {
        let html = ""
        for (const site of sites) {
           html += `<a href="#editSite/${site.title}/edit">${site.title}</a>`
        }
        return html
    }
    
    get template(){
        return `

        <div class="dropdown-right">
            <span>List of editors</span>
             <div class="dropdown-right-content">
            <p>${this.siteLinks(this.data)}</p>
            </div>
        </div>

            <div class="admin-box">
                <h1>Edit site</h1>

                <form id="logout" method="delete">
                <input type="submit" class="Edit" value="Logout"/>
                </form>
                <form id="main">
                <input type="submit"class="Edit" value="Main page"/>
                </form>
                <form id="pages">
                <input type="submit" class="Edit" value="Pages"/>
                </form>
                <form id="addEditor" method="post">
                <input type="text" id="editor" class="resizedTextBox">
                <input type="submit" class="Edit" value="Add editor"
                </form>
                <form id="deleteSite" method="delete">
                <input type="submit" class="Edit" value="Delete site"/>
                </form>

                    <form id ="editSite" method="put">  
                    <div class="editing">
                    <span>Title</span>
                    <input type="text" id="title" placeholder="">
    
                    <label>Description</label>
                    <input type="text" id="description" placeholder="">
    
                    <label>Style color</label>
                    <input type="text" id="colorInputText">
                    <input type="color" id="colorInputColor">
                    <input type="button" Class="Upload" value="Submit"
                    onclick="changeColor()">
    
                    <label>Font</label>
                    <input type="text" id="fontInput">
                    <input type="submit" class="Submit" value="Update">
                    </form>  
                    </div>

                        <script>
                        function changeColor(){
                            let color = document.getElementById('colorInputColor').value;
                            document.getElementById('colorInputText').value = color;
                        }
    
                        function changeFont(){
                            let font = document.getElementById('fontInput').value;
                            document.body.style.font = font;
                        }
                        </script>
           
            


        `
    }
}