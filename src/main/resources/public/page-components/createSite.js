class CreateSite extends Component{

    events(){
        $('body').on('submit', '#logout', this.logout)
        $('body').on('submit', '#createSite', this.createSite)
        $('body').on('submit', '#main', this.main)
    }

    async createSite(event){
         event.preventDefault()
        // url/site/create
         let result = await fetch(apiHost + '/site/create', {
             method: 'post',
             headers: { 'Content-Type': 'application/json' },
             body: JSON.stringify({
                 "title": document.querySelector('#title').value,
                 "description": document.querySelector('#description').value,
                 "colorTheme": document.querySelector('#colorInputText').value,
                 "font": document.querySelector('#fontInput').value
             })
         })
         let data = await result.json()
         console.log(result, data)
         if(result.status === 200){ // goes to new site if login status is 200 (200 = successful login)
            location.hash = "editSite"
        }

     }

    async preview(e){
        if(e.target.files.length > 0){
            this.file = e.target.files[0]
            let preview = document.getElementById("upload-preview")
            preview.src = URL.createObjectURL(this.file)
            preview.style.display = "block"       
        }
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

    get template(){
    return `
    <div class="admin-box">
        <h1>Create Site</h1>

        <form id="logout" method="delete">
        <input type="submit" class="Edit" value="Logout"/>
        </form>
        <form id="main">
        <input type="submit"class="Edit" value="Main page"/>
        </form>
                <form id= "createsite">
                <input type="logout" class="Logout" value="Logout">
                <div class="createsitediv">

                <span>Title</span>
                <input type="text" id="title" placeholder="">

                <label>Description</label>
                <input type="text id="description" placeholder="">
                </form>

                <label>Style color</label>
                <input type="text" id="colorInputText">
                <input type="color" id="colorInputColor">
                <input type="submit" id="colorTheme" Class="Upload" value="Submit"
                onclick="changeColor()">

                <label>Font</label>
                <input type="text" id="fontInput">
                <input type="submit" class="Submit" value="Create">  
                </div>  
                </form>
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