##Autor: Daniel Otero Cárdenas

#--------------------------------------------------------------------
#-------------------------Data (static)------------------------------
#--------------------------------------------------------------------
#Real NumOut
OUT_Real <- read_excel("./data/simulation/OUT_Real.xlsx", col_names = FALSE)
data_out <- OUT_Real[-c(1:3),-1]

#Real people in building
# building_Real <- read_excel("data_processing/raw_data/SD Feb-May 2019.xlsx", sheet = "Promedios")
# building_Real$periodo <- building_Real$periodo - 70
# building_Real <- building_Real[-nrow(building_Real),]
# building_Real$Type <- "Real"

#--------------------------------------------------------------------
#------------------------Data (dynamic)------------------------------
#--------------------------------------------------------------------

code <-  readRDS("fldr.Rds")
path <- paste0("./data/output/", code,"/")
time_periods <- (sprintf("%s:%s", floor(seq(12,36)/2),c(rep(c("00","30"),12),"00")))
time_values <- (as.integer(seq(0,6*24,6) + 2))

#NumOut and times
times <- read.csv(paste0(path, "tallies M.txt"), sep = "\t")
numOut <- read.csv(paste0(path, "NumberOut M.txt"), sep = "\t")
numOut$Type <- "Simulación"
numOut <- numOut[,-2]
for(i in 1:ncol(data_out)){
  temp <- data.frame(c(0:151),data_out[,i])
  temp$Type <- "Real"
  names(temp) <- names(numOut)
  numOut <- rbind(numOut, temp)
}

#Waiting lines
wl <- (read.csv(paste0(path, "waitingLines M.txt"), sep = "\t"))
wl <- wl[,-1]
wl$Floor <- as.numeric(wl$Floor)

#First floor
ff <- (read.csv(paste0(path, "firstFloor M.txt"), sep = "\t"))
ff <- ff[,-c(1,2)]

#Building
peopleBuilding <- (read.csv(paste0(path, "numberInBuilding M.txt"), sep = "\t"))
peopleBuilding$Type <- "Simulación"

#Usage
usage <- (read.csv(paste0(path, "Elevator Usage.txt"),sep = "\t"))
usage$Elevator.ID <- as.factor(usage$Elevator.ID)

#Confidence interval
reps <- max(times$Rep)
alpha <- 0.05

#--------------------------------------------------------------------
#------------------------Confidence interval-------------------------
#--------------------------------------------------------------------
half_width <- function(matrix, type = "S"){
  res <- c()
  for(i in 0:150){
    vector <- matrix[matrix[,1] == i,2]
    if (type == "S")
      val <- qt(1 - alpha/2,reps - 1) * sd(vector)/sqrt(reps)
    else
      val <- qt(1 - alpha/2,reps - 1) * sd(vector)/sqrt(38)
    res[i+1] <- val
  }
  
  return(res)
}

#--------------------------------------------------------------------
#-----------------------------Tallies--------------------------------
#--------------------------------------------------------------------

tallies <- function(origin, destination, type){
  
  red <- times[times$Origin == origin & times$Destination == destination,]
  red <- red[,c("Period", "Origin", "Destination", "Mode", "Average")]
  
  avg <- aggregate(Average ~.,data = red, mean)
  
  if (type == "box"){
    p <- plot_ly(red, y = red$Average, x = red$Period, color = red$Mode, colors = "Set1", type = "box")
    p <- add_trace(p,y = avg$Average, x = avg$Period, color = avg$Mode, colors = "Set1", type = "scatter", mode = "lines")
    
  } else if (type == "ci") {
    hw1 <- half_width(red[red$Mode == "Stairs",c(1,5)])
    hw2 <- half_width(red[red$Mode == "Elevator",c(1,5)])
    temp <- avg[avg$Mode == "Stairs",]
    p <- plot_ly(temp, y = temp$Average, x = temp$Period, type = "scatter", name = "Stairs",
                 error_y = list(
                   array = hw1
                 )
    )
    temp <- avg[avg$Mode == "Elevator",]
    p <- add_trace(p,temp, y = temp$Average, x = temp$Period, type = "scatter", name = "Elevator",
                   error_y = list(
                     array = hw2
                   )
    )
  }
  
  p <- p %>% layout(
    colorway = brewer.pal(3,"Set1")[c(2,1)],
    xaxis = list(
      title = "Hora del día",
      ticktext = time_periods,
      tickvals = time_values,
      tickmode = "array",
      tickangle = 90,
      showgrid = TRUE
    ),
    yaxis = list(
      title = "Minutos"
    ),
    title = sprintf("Tiempo de desplazamiento entre los pisos#%s y#%s", origin, destination)
  )
  
  return(p)
}

#--------------------------------------------------------------------
#-----------------------------Number out-----------------------------
#--------------------------------------------------------------------

numberOut <- function(type){
  
  avg <- aggregate(Number ~.,data = numOut, mean)
  
  if (type == "box"){
    p <- plot_ly(y = numOut$Number, x = numOut$Period, color = numOut$Type, colors = "Set1", type = "box")
    p <- add_trace(p,y = avg$Number, x = avg$Period, color = avg$Type, colors = "Set1", type = "scatter", mode = "lines", showlegend = FALSE)
    
  } else if (type == "ci") {
    hw1 <- half_width(numOut[numOut$Type == "Simulación",c(1,2)])
    temp <- avg[avg$Type == "Simulación",]
    p <- plot_ly(temp, y = temp$Number, x = temp$Period, type = "scatter", name = "Simulación",
                 error_y = list(
                   array = hw1
                 )
    )
    
    hw2 <- half_width(numOut[numOut$Type == "Real",c(1,2)],"R")
    temp <- avg[avg$Type == "Real",]
    p <- add_trace(p,temp, y = temp$Number, x = temp$Period, type = "scatter", name = "Real",
                   error_y = list(
                     array = hw2
                   )
    )
  }  
  
  p <- p %>% layout(
    colorway = brewer.pal(2,"Set1")[c(2,1)],
    xaxis = list(
      title = "Hora del día",
      ticktext = time_periods,
      tickvals = time_values,
      tickmode = "array",
      tickangle = 90,
      showgrid = TRUE
    ),
    yaxis = list(
      title = "Número de personas saliendo"
    ),
    title = "Cantidad de personas saliendo del edificio a diferentes momentos del día"
  )
  
  return(p)
  
}

#--------------------------------------------------------------------
#--------------------------Waiting Lines-----------------------------
#--------------------------------------------------------------------
#Mixed
waitingLines <- function(type, floor){
  temp <- wl[wl$Floor %in% floor,]
  temp$Floor <- as.factor(temp$Floor)
  avg <- aggregate(Number~., data = temp, mean)
  #avg$Floor <- as.factor(avg$Floor)
  
  
  if (type == "box"){
    p <- plot_ly(temp,y = temp$Number, x = temp$Period, color = temp$Floor, colors = "Set1", type = "box")
    p <- add_trace(p,y = avg$Number, x = avg$Period, color = avg$Floor, colors = "Set1", type = "scatter", mode = "lines")
    
  } else if (type == "ci") {
    p <- plot_ly(colors = "Set1")
    for(i in floor){
      hw1 <- half_width(temp[temp$Floor == i,c(2,3)])
      avg2 <- avg[avg$Floor == i,]
      p <- add_trace(p,y = avg2$Number, x = avg2$Period, type = "scatter", name = i,
                     error_y = list(
                       array = hw1
                     )
      )
    }
  }
  
  if (length(floor) > 1){
    tit = ""
    for (i in 1:length(floor)){
      tit <- paste0(tit,floor[i])
      if (i != length(floor)){
        tit <- paste0(tit,", ")
      }
      
    }
    
    tit = paste0("Tamaño de la fila en los pisos ",tit," durante el día")
  } else{
    tit = paste0("Tamaño de la fila en el piso ",floor[1]," durante el día")
  }
  
  p <- p %>% layout(
    colorway = brewer.pal(2,"Set1")[c(2,1)],
    xaxis = list(
      title = "Hora del día",
      ticktext = time_periods,
      tickvals = time_values,
      tickmode = "array",
      tickangle = 90,
      showgrid = TRUE
    ),
    yaxis = list(
      title = "Número de personas"
    ),
    title = tit
  )
  return(p)
}

#--------------------------------------------------------------------
#-------------------------- First floor -----------------------------
#--------------------------------------------------------------------
#Mixed
FirstFloor <- function(types){
  avg <- aggregate(Number~., data = ff, max)
  p <- plot_ly(type = "bar")  
  if ("Traveling" %in% types){
    temp <- avg[avg$Type == 4,]
    p <- add_trace(p,y = temp$Number, x = temp$Period, name = "Traveling")
  }
  if ("Elevator" %in% types){
    temp <- avg[avg$Type == 2,]
    p <- add_trace(p,y = temp$Number, x = temp$Period, name = "Elevator waiting line")
  }
  if ("Turnstile" %in% types){
    temp <- avg[avg$Type == 3,]
    p <- add_trace(p,y = temp$Number, x = temp$Period, "Turnstiles", name = "Exiting at turnstiles")
  }
  tit = "People on the first floor"
  
  p <- p %>% layout(
    colorway = brewer.pal(3,"Set1"),
    xaxis = list(
      title = "Time",
      ticktext = time_periods,
      tickvals = time_values,
      tickmode = "array",
      tickangle = 90,
      showgrid = TRUE
    ),
    yaxis = list(
      title = "Average number of people"
    ),
    title = tit, 
    barmode = "stack"
  )
  return(p)
}


#--------------------------------------------------------------------
#--------------------------In building-------------------------------
#--------------------------------------------------------------------

building <- function(type){
  temp <- peopleBuilding[,-1]
  # for (i in 1:(ncol(building_Real)-2)){
  #   temp2 <- building_Real[,c(1,i + 1,ncol(building_Real))]
  #   names(temp2) <- names(temp)
  #   temp <- rbind(temp,temp2)
  # }
  avg <- aggregate(Number~., data = temp, mean)
  
  if (type == "box"){
    p <- plot_ly(y = temp$Number, x = temp$Period, color = temp$Type, colors = "Set1", type = "box")
    p <- add_trace(p,y = avg$Number, x = avg$Period, color = avg$Type, colors = "Set1", type = "scatter", mode = "lines")
    
  } else if (type == "ci") {
    hw1 <- half_width(temp[temp$Type == "Simulación",c(1,2)])
    hw2 <- half_width(temp[temp$Type == "Real",c(1,2)], "R")
    temp2 <- avg[avg$Type == "Simulación",]
    p <- plot_ly( y = temp2$Number, x = temp2$Period, type = "scatter", name = "Simulación",
                 error_y = list(
                   array = hw1
                 )
    )
    temp2 <- avg[avg$Type == "Real",]
    p <- add_trace(p, y = temp2$Number, x = temp2$Period, type = "scatter", name = "Real", 
                   error_y = list(
                     array = hw2
                   )
    )
  }
  
  p <- p %>% layout(
    colorway = brewer.pal(2,"Set1")[c(2,1)],
    xaxis = list(
      title = "Hora del día",
      ticktext = time_periods,
      tickvals = time_values,
      tickmode = "array",
      tickangle = 90,
      showgrid = TRUE
    ),
    yaxis = list(
      title = "Número de personas"
    ),
    title = "Cantidad de personas dentro del edificio a diferentes horas del día"
  )
  
  return(p)
}
#--------------------------------------------------------------------
#---------------------------Utilization------------------------------
#--------------------------------------------------------------------
utilization <- function(type){
  avg <- aggregate(Usage~., data = usage[,-3], mean)
  
  if (type == "box"){
    p <- plot_ly(y = usage$Usage, x = usage$Period, color = usage$Elevator.ID, colors = "Set1", type = "box")
    p <- add_trace(p,y = avg$Usage, x = avg$Period, color = avg$Elevator.ID, colors = "Set1", type = "scatter", mode = "lines")
    
  } else if (type == "ci") {
    p <- plot_ly()
    for(i in 1:4){
      temp <- avg[avg$Elevator.ID == i,]
      hw1 <- half_width(usage[,c(2,4)])
      p <- add_trace(p, y = temp$Usage, x = temp$Period, type = "scatter", name = paste0("Ascensor ", i),
                     error_y = list(
                       array = hw1
                     )
      )
    }
  }
  
  p <- p %>% layout(
    colorway = brewer.pal(2,"Set1"),
    xaxis = list(
      title = "Hora del día",
      ticktext = time_periods,
      tickvals = time_values,
      tickmode = "array",
      tickangle = 90,
      showgrid = TRUE
    ),
    yaxis = list(
      title = "Porcentaje"
    ),
    title = "Utilización de los ascensores a diferentes horas del día"
  )
  
  return(p)
}

#--------------------------------------------------------------------
#--------------------------Other stuff-------------------------------
#--------------------------------------------------------------------

progress <- function(path){
  prog <- tryCatch(
    {
      nrow(read.csv(paste0(path, "progress.txt")))
    },
    error = function(cond){
      return(0)
    })
  return(prog)
}

#Write elevator data on a .txt file
writetxt <- function(n,p,e,o){
  x <- data.frame()
  x[1,1] <- "N"
  x[1,2] <- n
  x[2,1] <- "X"
  x[2,2] <- p
  x[3,1] <- "E"
  x[3,2] <- e
  x[4,1] <- "O"
  x[4,2] <- o
  write.table(x, file = "./data/simulation/Elevators.txt", row.names = FALSE, sep = "\t", quote = FALSE)
}

#calculate the time of a period
time <- function(period){
  periods <- c("5:50","5:55",(sprintf("%s:%s", floor(seq(72,72 * 4)/12),c(rep(c("00","05","10","15","20","25","30","35","40","45","50","55"),216/12),"00"))))
  return(periods[period + 1])
}

readCode <- function(){
  c <- read.csv("./data/simulation/Elevators.txt", sep = "\t")
  t <- ""
  for(i in 1:4){
    t <- paste0(t,c[i,1],"-",c[i,2],"_")
  }
  return(t)
}

#Gives numbers of the path
nombre <- function(ruta){
  partes <- as.data.frame(strsplit(ruta, "_"))
  asc <- as.data.frame(apply(partes, 1, function (x) strsplit(x,"-")))
  str <- paste0(asc[2,1], asc[2,2], asc[2,3], asc[2,4])
  return(str)
}
