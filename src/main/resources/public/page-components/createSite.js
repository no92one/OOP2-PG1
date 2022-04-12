class CreateSite extends Component{

    events(){
        $('body').on('submit', '#logout', this.logout)
    }

    // async createSite(event){
    //     event.preventDefault()
        
    //     let result = await fetch(apiHost + '/api/auth/createSite', {
    //         method: 'post',
    //         headers: { 'Content-Type': 'application/json' },
    //         body: JSON.stringify({
    //             "site": document.querySelector('#site-name').value,
    //             "title": document.querySelector('#title').value,
    //         })
    //     })
    //     let data = await result.json()
    //     console.log(result, data)
    //     site = data;
    // }

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

       <h1>Create Site</h1>
       <form id="logout" method="delete">
       <input type="submit" class="Submit" value="Logout"/>
       <input type="submit" class="Submit" value="Main page"/>
       </form>
            <div class="createsite-block">

                <form id="createsite">
                    <label>Site name</label>
                    <input type="text" id="site-name" placeholder="">
                    <label>Title</label>
                    <input type="email" id="title" placeholder="">
                    <label>Description</label>
                    <input type="text" id="description" placeholder=""
                    <label>Upload logo</label>
                    <label>Upload wallpaper</label>
                    <input type="submit" class="Submit" value="Create">
                    </div>
                    </form>
                
            
        `

    }

}