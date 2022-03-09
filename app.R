#Packages

if(!require("readxl")) install.packages("readxl")
if(!require("shinyWidgets")) install.packages("shinyWidgets")
if(!require("shiny")) install.packages("shiny")
if(!require("shinydashboard")) install.packages("shinydashboard")
if(!require("plotly")) install.packages("plotly")
if(!require("scales")) install.packages("scales")

library(readxl)
library(shinyWidgets)
library(shiny)
library(shinydashboard)
library(plotly)
library(scales)
library(RColorBrewer)

Sys.setlocale("LC_ALL", "Spanish")
options(encoding = "UTF-8")

#Which folder to read
code <- "N-4_X-0_E-0_O-0_"
saveRDS(code, file = "fldr.Rds")

#Source back script
source("Back.R")

#--------------------------------------------------------------------
#-------------------------------ui-----------------------------------
#--------------------------------------------------------------------
ui <- dashboardPage(
    dashboardHeader(title = textOutput("Title")),
    ##----------------------------------------------
    #----------------Sidebar content----------------
    #-----------------------------------------------
    dashboardSidebar(
        sidebarMenu(
            menuItem("Personas que salen", tabName = "salida"),
            menuItem("Personas en edificio", tabName = "personas"),
            menuItem("Tiempo de desplazamiento", tabName = "desplazamiento"),
            menuItem("Ascensores", tabName = "ascensores",
                     menuSubItem("Personas en fila", tabName = "espera"),
                     menuSubItem("Utilización ascensores", tabName = "utilizacion"),
                     menuSubItem("Modificar ascensores", tabName = "modificar")
            )
            #switchInput(label = "¿Todos?", inputId = "Estudiantes y profesores", value = TRUE)
        )
    ),
    ##----------------------------------------------
    #-------------------Body content----------------
    #-----------------------------------------------
    dashboardBody(
        tabItems(
            ##-------------------------------------
            #----------Salida de personas----------
            #--------------------------------------
            tabItem(
                tabName = "salida",
                # First row of stuff
                fluidRow(
                    h2("Personas que salen en diferentes momentos del día"),
                    # Boxes
                    column(
                        width = 5,
                        valueBoxOutput("salida_b1", width = 12),
                        valueBoxOutput("salida_b2", width = 12),
                        valueBoxOutput("salida_b3", width = 12),
                        valueBoxOutput("salida_b4", width = 12),
                    ),
                    # Graph with multiple tabs
                    tabBox(
                        width = 7,
                        title = "Número de personas saliendo del edificio",
                        id = "personas_t3",
                        # First tab
                        tabPanel(
                            title = "Box plots",
                            plotlyOutput(
                                "saliendo_bp"
                            )
                        ),
                        # Second tab
                        tabPanel(
                            title = "Intervalos",
                            plotlyOutput("saliendo_CI")
                        )
                    )
                )
            ),
            ##-------------------------------------
            #----------personas en edificio--------
            #--------------------------------------
            tabItem(
                tabName = "personas",
                # First row of stuff
                fluidRow(
                    h2("Personas dentro del edificio en diferentes momentos del día"),
                    # Boxes
                    column(
                        width = 5,
                        valueBoxOutput("personas_b1", width = 12),
                        valueBoxOutput("personas_b2", width = 12),
                        
                        valueBoxOutput("personas_b3", width = 12),
                        valueBoxOutput("personas_b4", width = 12)
                    ),
                    # Graph with multiple tabs
                    tabBox(
                        width = 7,
                        title = "Número de personas en el edificio",
                        id = "personas_t1",
                        # First tab
                        tabPanel(
                            title = "Box plots",
                            plotlyOutput(
                                "personas_bp"
                            )
                        ),
                        # Second tab
                        tabPanel(
                            title = "Intervalos",
                            plotlyOutput("personas_CI")
                        )
                    )
                )
            ),
            
            ##-------------------------------------
            #--------------Desplazamiento----------
            #--------------------------------------
            tabItem(
                tabName = "desplazamiento",
                # First row of stuff
                fluidRow(
                    h2("Tiempo de desplazamiento por modo de transporte entre un origen y un destino"),
                    column(
                        width = 5,
                        #Inputs
                        fluidRow(
                            box(
                                width = 12,
                                h3("Origen - Destino"),
                                sliderInput("desplazamiento_inicial", h4("Piso origen:"), 1, min = 0, max = 10),
                                sliderInput("desplazamiento_final", h4("Piso destino:"), 8, min = 0, max = 10)
                            ),
                            # box(
                            #     width = 6,
                            #     h3("Modo de transporte"),
                            #     radioButtons("desplazamiento_modo", label = h4("Modo"),
                            #                  choices = list("Escaleras" = 1, "Ascensores" = 2, "Mixto" = 3), 
                            #                  selected = 3)
                            # )
                        ),
                    ),
                    # Graph with multiple tabs
                    tabBox(
                        width = 7,
                        title = "Tiempo de desplazamiento",
                        id = "desplazamiento_t1",
                        # First tab
                        tabPanel(
                            title = "Box plots",
                            plotlyOutput("desplazamiento_bp")
                        ),
                        # Second tab
                        tabPanel(
                            title = "Intervalos",
                            plotlyOutput("desplazamiento_CI")
                        )
                    )
                ),
                fluidRow(
                    column(
                        width = 6,
                        #Boxes
                        valueBoxOutput("desplazamiento_b1", width = 12),
                        valueBoxOutput("desplazamiento_b2", width = 12),
                        
                    ),
                    column(
                        width = 6,
                        #Boxes
                        valueBoxOutput("desplazamiento_b3", width = 12),
                        valueBoxOutput("desplazamiento_b4", width = 12)
                    )
                )
            ),
            ##-------------------------------------
            #----------Tiempos de espera-----------
            #--------------------------------------
            tabItem(
                tabName = "espera",
                # First row of stuff
                fluidRow(
                    h2("Promedio de personas en espera en diferentes momentos del día"),
                    # Boxes
                    column(
                        width = 5,
                        box(
                            width = 12,
                            checkboxGroupInput("checkGroup", label = h3("Seleccione los pisos que desee ver"), 
                                               choices = list(
                                                   "Piso 1" = 1, "Piso 2" = 2, "Piso 3" = 3,
                                                   "Piso 4" = 4, "Piso 5" = 5, "Piso 6" = 6,
                                                   "Piso 7" = 7, "Piso 8" = 8, "Piso 9" = 9,
                                                   "Piso 10" = 10
                                               ),
                                               selected = 1),
                        ),
                        valueBoxOutput("espera_b1", width = 12),
                        valueBoxOutput("espera_b2", width = 12),
                        valueBoxOutput("espera_b3", width = 12)
                        
                    ),
                    # Graph with multiple tabs
                    tabBox(
                        width = 7,
                        title = "Número de personas en el edificio",
                        id = "personas_t1",
                        # First tab
                        tabPanel(
                            title = "Box plots",
                            plotlyOutput(
                                "espera_bp"
                                #height = "400px"
                            )
                        ),
                        # Second tab
                        tabPanel(
                            title = "Intervalos",
                            plotlyOutput("espera_CI")
                        )
                    )
                )
            ),
            ##-------------------------------------
            #-------------Utilización--------------
            #--------------------------------------
            tabItem(
                tabName = "utilizacion",
                # First row of stuff
                fluidRow(
                    h2("Personas dentro del edificio en diferentes momentos del día"),
                    # Boxes
                    column(
                        width = 5,
                        valueBoxOutput("utilizacion_b1", width = 12),
                        valueBoxOutput("utilizacion_b2", width = 12)
                    ),
                    # Graph with multiple tabs
                    tabBox(
                        width = 7,
                        title = "# de personas en el edificio",
                        id = "personas_t1",
                        # First tab
                        tabPanel(
                            title = "Box plots",
                            plotlyOutput(
                                "utilizacion_bp"
                                #height = "400px"
                            )
                        ),
                        # Second tab
                        tabPanel(
                            title = "Intervalos",
                            plotlyOutput("utilizacion_CI")
                        )
                    )
                )
            ),
            
            ##-------------------------------------
            #--------Modificar ascensores----------
            #--------------------------------------
            tabItem(
                tabName = "modificar",
                # First row of stuff
                fluidRow(
                    box(
                        width = 3,
                        h3("Lecheros"),
                        h4("Paran en todos los pisos"),
                        sliderInput("modificar_normales", h4("Ascensores:"), 4, min = 0, max = 4)
                    ),
                    box(
                        width = 3,
                        h3("Express"),
                        h4("Paran en los pisos: 7, 8, 9 y 10"),
                        sliderInput("modificar_express", h4("Ascensores:"), 0, min = 0, max = 4)
                    ),
                    box(
                        width = 3,
                        h3("Pares"),
                        h4("Paran en los pisos pares y el piso 1"),
                        sliderInput("modificar_pares", h4("Ascensores:"), 0, min = 0, max = 4)
                    ),
                    box(
                        width = 3,
                        h3("Impares"),
                        h4("Paran en los pisos impares"),
                        sliderInput("modificar_impares", h4("Ascensores:"), 0, min = 0, max = 4)
                    )
                ),
                fluidRow(
                    column(
                        width = 4
                    ),
                    column(
                        align = "center",
                        width = 4,
                        box(
                            width = 12,
                            div(h1("Presione el botón para actualizar simulación"),
                                style = "color:green"),
                            actionButton("actualizar", "Actualizar simulación")
                        )
                    ),
                    column(
                        width = 4
                    )
                ),
                fluidRow(
                )
            )
        )
    )
)

#--------------------------------------------------------------------
#---------------------------server-----------------------------------
#--------------------------------------------------------------------

server <- function(input, output, session) {
    
    #--------------------------------------
    #----------------Button----------------
    #--------------------------------------
    observeEvent(input$actualizar, {
        #Aquí modificar_normales hace referencia a los lecheros, y modificar_express a los express. 
        writetxt(input$modificar_normales, input$modificar_express, input$modificar_pares, input$modificar_impares)
        code <- readCode()
       
        saveRDS(code, file = "fldr.Rds")
        pathS <- paste0("./data/output/", code,"/")
        if (!dir.exists(pathS)){
            dir.create(pathS)
            shell.exec("Simulation.jar")
            Sys.sleep(10)
        }
        numReps <- progress(pathS)
        
        #Change header title
        output$Title <- renderText({
            paste0("Flujo Vertical SD ", nombre(code))
        })
        
        write.table("", paste0(pathS, "progress.txt"), row.names = FALSE, col.names = FALSE, quote = FALSE)
        shell.exec("Simulation.jar")
        withProgress(message = "Simulando", value = 0, {
            num <- 0
            while (num < numReps){
                
                new <- progress(pathS)
                incProgress((new-num)/numReps)
                num <- new
                print(num)
                Sys.sleep(1)
            }
        })
        print("SALE")
        #Sys.sleep(15)
        source("Back.R")
        
        #incProgress(1/13)
        
        output$personas_bp <- renderPlotly({
            building("box")
        })
        
        output$personas_CI <- renderPlotly({
            building("ci")
        })
        
        output$desplazamiento_bp <- renderPlotly({
            tallies(input$desplazamiento_inicial, input$desplazamiento_final,"box")
        })
        
        output$desplazamiento_CI <- renderPlotly({
            tallies(input$desplazamiento_inicial, input$desplazamiento_final,"ci")
        })
        
        output$saliendo_bp <- renderPlotly({
            numberOut("box")
        })
        
        output$saliendo_CI <- renderPlotly({
            numberOut("ci")
        })
        
        output$espera_bp <- renderPlotly({
            floors <- as.numeric(input$checkGroup)
            waitingLines("box", floors)
        })
        
        output$espera_CI <- renderPlotly({
            floors <- as.numeric(input$checkGroup)
            waitingLines("ci", floors)
        })
        
        output$utilizacion_bp <- renderPlotly({
            utilization("box")
        })
        
        output$utilizacion_CI <- renderPlotly({
            utilization("ci")
        })
        
        incProgress(1/13)

    })
    
    #Change header title
    output$Title <- renderText({
        paste0("Flujo Vertical SD ", nombre(code))
    })
    
    #--------------------------------------
    #----------------Graphs----------------
    #--------------------------------------
    
    output$personas_bp <- renderPlotly({
        building("box")
    })
    
    output$personas_CI <- renderPlotly({
        building("ci")
    })
    
    output$desplazamiento_bp <- renderPlotly({
        tallies(input$desplazamiento_inicial, input$desplazamiento_final,"box")
    })
    
    output$desplazamiento_CI <- renderPlotly({
        tallies(input$desplazamiento_inicial, input$desplazamiento_final,"ci")
    })
    
    output$saliendo_bp <- renderPlotly({
        numberOut("box")
    })
    
    output$saliendo_CI <- renderPlotly({
        numberOut("ci")
    })
    
    output$espera_bp <- renderPlotly({
        floors <- as.numeric(input$checkGroup)
        waitingLines("box", floors)
    })
    
    output$espera_CI <- renderPlotly({
        floors <- as.numeric(input$checkGroup)
        waitingLines("ci", floors)
    })
    
    output$utilizacion_bp <- renderPlotly({
        utilization("box")
    })
    
    output$utilizacion_CI <- renderPlotly({
        utilization("ci")
    })
    
    output$primer_piso_bc <- renderPlotly({
        FirstFloor(c("Traveling", "Elevator", "Turnstile"))
    })
    
    #--------------------------------------
    #----------------Boxes-----------------
    #--------------------------------------
    
    ## Boxes personas
    output$personas_b1 <- renderValueBox({
        valueBox(
            max(peopleBuilding$Number), "Máximo número de personas en el edificio: Simulación", icon = icon("list"),
            color = "blue"
        )
    })
    
    output$personas_b2 <- renderValueBox({
        valueBox(
            time(peopleBuilding[which.max(peopleBuilding$Number),"Period"]), "Hora en la que hay más personas en el edificio: Simulación",
            icon = icon("thumbs-up", lib = "glyphicon"),
            color = "blue"
        )
    })
    
    # output$personas_b3 <- renderValueBox({
    #     valueBox(
    #         max(as.matrix(na.omit(building_Real[,-c(1,ncol(building_Real))]))),
    #         "Máximo número promedio de personas en el edificio: Real", icon = icon("list"),
    #         color = "red"
    #     )
    # })
    # 
    # output$personas_b4 <- renderValueBox({
    #     valueBox(
    #         time(as.numeric(building_Real[
    #             which(as.matrix(na.omit(
    #                 building_Real[,-c(1,ncol(building_Real))])) == max(as.matrix(na.omit(building_Real[,-c(1,ncol(building_Real))]))), arr.ind = TRUE)[1] + 1, 1])),
    #         "Hora en la que hay más personas en promedio en el edificio: Real",
    #         icon = icon("thumbs-up", lib = "glyphicon"),
    #         color = "red"
    #     )
    # })
    
    ## Boxes desplazamiento
    output$desplazamiento_b1 <- renderValueBox({
        temp <- times[times$Mode == "Elevator" & times$Origin == input$desplazamiento_inicial & times$Destination == input$desplazamiento_final, ]
        valueBox(
            max(temp$Average), "Tiempo promedio máximo por ascensor", icon = icon("list"),
            color = "red"
        )
    })
    
    
    output$desplazamiento_b2 <- renderValueBox({
        temp <- times[times$Mode == "Elevator" & times$Origin == input$desplazamiento_inicial & times$Destination == input$desplazamiento_final, ]
        valueBox(
            time(temp[which.max(temp$Average),4]), "Hora a la que es más demorado", icon = icon("thumbs-up", lib = "glyphicon"),
            color = "red"
        )
    })
    
    output$desplazamiento_b3 <- renderValueBox({
        temp <- times[times$Mode == "Stairs" & times$Origin == input$desplazamiento_inicial & times$Destination == input$desplazamiento_final, ]
        valueBox(
            max(temp$Average), "Tiempo promedio máximo por escaleras", icon = icon("list"),
            color = "blue"
        )
    })
    
    
    output$desplazamiento_b4 <- renderValueBox({
        temp <- times[times$Mode == "Stairs" & times$Origin == input$desplazamiento_inicial & times$Destination == input$desplazamiento_final, ]
        valueBox(
            time(temp[which.max(temp$Average),4]), "Hora a la que es más demorado", icon = icon("thumbs-up", lib = "glyphicon"),
            color = "blue"
        )
    })
    
    ## Boxes salida
    output$salida_b1 <- renderValueBox({
        temp <- numOut[numOut$Type == "Real", ]
        valueBox(
            max(temp$Number), "Máxima salida de personas en el modelo real", icon = icon("list"),
            color = "red"
        )
    })
    
    
    output$salida_b2 <- renderValueBox({
        temp <- numOut[numOut$Type == "Real", ]
        valueBox(
            time(temp[which.max(temp$Number),1]), "Hora a la que salen más personas en el modelo real", icon = icon("thumbs-up", lib = "glyphicon"),
            color = "red"
        )
    })
    
    output$salida_b3 <- renderValueBox({
        temp <- numOut[numOut$Type == "Simulación", ]
        valueBox(
            max(temp$Number), "Máxima salida de personas en el modelo simulado", icon = icon("list"),
            color = "blue"
        )
    })
    
    
    output$salida_b4 <- renderValueBox({
        temp <- numOut[numOut$Type == "Simulación", ]
        valueBox(
            time(temp[which.max(temp$Number),1]), "Hora a la que salen más personas en el modelo simulado", icon = icon("thumbs-up", lib = "glyphicon"),
            color = "blue"
        )
    })
    
    ## Boxes espera
    output$espera_b1 <- renderValueBox({
        floors <- as.numeric(input$checkGroup)
        temp <- wl[wl$Floor %in% floors, ]
        valueBox(
            max(temp$Number), "Máximo número de personas en fila", icon = icon("list"),
            color = "blue"
        )
    })
    
    
    output$espera_b2 <- renderValueBox({
        floors <- as.numeric(input$checkGroup)
        temp <- wl[wl$Floor %in% floors, ]
        valueBox(
            time(temp[which.max(temp$Number),"Period"]), "Hora a la que hay más fila", icon = icon("thumbs-up", lib = "glyphicon"),
            color = "red"
        )
    })
    
    output$espera_b3 <- renderValueBox({
        floors <- as.numeric(input$checkGroup)
        temp <- wl[wl$Floor %in% floors, ]
        valueBox(
            (temp[which.max(temp$Number),"Floor"]), "Piso en el que hay más fila (entre los seleccionados)", icon = icon("thumbs-up", lib = "glyphicon"),
            color = "green"
        )
    })
    
    ## Boxes utilización
    output$utilizacion_b1 <- renderValueBox({
        valueBox(
            percent(max(usage$Usage)), "Utilización máxima", icon = icon("list"),
            color = "blue"
        )
    })
    
    
    output$utilizacion_b2 <- renderValueBox({
        valueBox(
            time(usage[which.max(usage$Usage),"Period"]), "Hora a la que hay mayor utilización", icon = icon("thumbs-up", lib = "glyphicon"),
            color = "red"
        )
    })
    
    #--------------------------------------
    #----------------Exit------------------
    #--------------------------------------
    
    session$onSessionEnded(function() {
        print('hello, the session has ended')
    })
    
    
}

shinyApp(ui, server)
