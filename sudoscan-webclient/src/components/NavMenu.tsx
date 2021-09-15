export const NavMenu = () => { 

    return (
        <nav className="bg-blue-400">
            <div className="max-w-7xl mx-auto px-2 sm:px-6 lg:px-8">
                <div className="relative flex items-center justify-between h-16">       
                    <div className="flex-1 flex items-center justify-center sm:items-stretch sm:justify-start font-mono">
                        <div className="flex-shrink-0 flex items-center">
                            <img className="hidden lg:block h-8 w-auto" src="./sudoku.svg" alt="Workflow"/>                            
                            <span className=" px-4">Sudoscan</span>
                        </div>
                        {/* <div className="hidden sm:block sm:ml-6">
                            <div className="flex space-x-4">
                                <a href="#" className="bg-blue-400 text-white px-3 py-2 rounded-md text-sm " aria-current="page">Picture</a>
                            </div>
                        </div> */}
                    </div>
                </div>
            </div>
        </nav>
    )

}