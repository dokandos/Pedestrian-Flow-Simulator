#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------
#                               Funciones
#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------

#--------------------------------------------------------------------
#                         Revisar outliers
#--------------------------------------------------------------------
revisarOutliers <- function(file){
  resp <- c()
  prog <- read.csv(paste0(file, "/progress", ".txt"), sep = ":")
  for(i in 1:length(prog)){
    num <- prog[i,1]
    if(num > 1000){
      resp <- c(resp,i)
    }
  }
  return (resp)
}

#--------------------------------------------------------------------
#                       Confidence interval
#--------------------------------------------------------------------
half_width <- function(matrix, reps){
  res <- c()
  for(i in 0:150){
    vector <- matrix[matrix[,1] == i,2]
    val <- qt(1 - alpha/2,reps - 1) * sd(vector)/sqrt(reps)
    val <- ifelse(is.na(val),0,val)
    res[i+1] <- val
  }
  
  return(res)
}

#--------------------------------------------------------------------
#                            Overlapping
#--------------------------------------------------------------------
#Positivo si A es mayor (B mejor que A), negativo si B es mayor (a mejor que B), 0 si se sobrelapan
overlap <- function(meanA, hwA, meanB, hwB){
  if(length(meanA) == 0 | length(meanB) == 0){
    return(NA)
  }
  
  if (meanA > meanB){
    if (meanA - hwA < meanB + hwB){
      return(0)
    } else{
      return(meanA - hwA - (meanB + hwB))
    }
  } else if (meanA < meanB){
    if (meanA + hwA > meanB - hwB){
      return(0)
    } else {
      return(meanA + hwA - (meanB - hwB))
    }
  }else if (meanA == meanB){
    return(0)
  }else{
    return(NA)
  }
}

#--------------------------------------------------------------------
#                 Cod para el título de las gráficas
#--------------------------------------------------------------------
codigo <- function(ruta){
  partes <- as.data.frame(strsplit(ruta, "_"))
  asc <- as.data.frame(apply(partes, 1, function (x) strsplit(x,"-")))
  str <- paste0("Norm: ", asc[2,1], ", Exp: ", asc[2,2], ", Par: ", asc[2,3], ", Impar: ", asc[2,4], ". ")
  return(str)
}

#--------------------------------------------------------------------
#                 Cod para el nombre de las gráficas
#--------------------------------------------------------------------
nombre <- function(ruta){
  if (ruta == "Original"){
    return(ruta)
  } else{
    partes <- as.data.frame(strsplit(ruta, "_"))
    asc <- as.data.frame(apply(partes, 1, function (x) strsplit(x,"-")))
    str <- paste0(asc[2,1], asc[2,2], asc[2,3], asc[2,4])
    return(str)
  }
}

#--------------------------------------------------------------------
#                             Comparar
#--------------------------------------------------------------------
#V1
comparar <- function(a,b){
  
  aa <- substr(a, 15, nchar(a))
  bb <- substr(b, 15, nchar(b))
  
  ta <- read.csv(paste0(a, "/", "tallies M.txt"), sep = "\t")
  tb <- read.csv(paste0(b, "/", "tallies M.txt"), sep = "\t")
  
  outliersA <- revisarOutliers(a)
  outliersB <- revisarOutliers(b)
  
  if (length(outliersA)/max(ta$Rep)<=0.5 & length(outliersB)/max(tb$Rep)<=0.5){
    
    origenes <- c(1,1,7,8)
    destinos <- c(7,8,1,1)
    for(i in 1:4){
      origen = origenes[i]
      destino = destinos[i]
      ra <- ta[ta$Origin == origen & ta$Destination == destino,]
      if (!is.null(outliersA))
        ra <- ra[!ra$Rep %in% outliersA,]
      ra <- ra[,c("Period", "Mode", "Average")]
      
      rb <- tb[tb$Origin == origen & tb$Destination == destino,]
      if (!is.null(outliersB))
        rb <- rb[!rb$Rep %in% outliersB,]
      rb <- rb[,c("Period", "Mode", "Average")]
      
      avgA <- aggregate(Average ~.,data = ra, mean)
      avgB <- aggregate(Average ~.,data = rb, mean)
      
      hwEscA <- half_width(ra[ra$Mode == "Stairs", c(1,3)])
      hwAscA <- half_width(ra[ra$Mode == "Elevator", c(1,3)])
      
      hwEscB <- half_width(rb[rb$Mode == "Stairs", c(1,3)])
      hwAscB <- half_width(rb[rb$Mode == "Elevator", c(1,3)])
      
      elevs <- data.frame()
      stairs <- data.frame()
      for(j in 0:(length(hwAscA) - 1)){
        medEscA <- avgA[avgA$Period == j & avgA$Mode == "Stairs",c("Average")]
        medAscA <- avgA[avgA$Period == j & avgA$Mode == "Elevator",c("Average")]
        
        medEscB <- avgB[avgB$Period == j & avgB$Mode == "Stairs",c("Average")]
        medAscB<- avgB[avgB$Period == j & avgB$Mode == "Elevator",c("Average")]
        
        stairs[j + 1, 1] <- "Escaleras"
        stairs[j + 1, 2] <- j
        stairs[j + 1, 3] <- overlap(medEscA, hwEscA[j + 1], medEscB, hwEscB[j + 1])
        
        elevs[j + 1, 1] <- "Ascensores"
        elevs[j + 1, 2] <- j
        elevs[j + 1, 3] <- overlap(medAscA, hwAscA[j + 1], medAscB, hwAscB[j + 1])
      }
      
      total <- rbind(stairs,elevs)
      names(total) <- c("Modo", "Periodo", "Valor")
      total$Valor <- as.numeric(total$Valor)
      #sum(total$Valor < 0, na.rm = T)/nrow(total) > 0.95 && 
      #if (sum(total$Valor == 0, na.rm = T)/nrow(total) < 0.95 & sum(total$Valor < 0, na.rm = T) < sum(total$Valor > 0, na.rm = T) ){
      if (sum(total$Valor < 0, na.rm = T) == 0 ){
        p <- ggplot(data=total, aes(x=Periodo, y=Valor, group=Modo)) +
          geom_line(aes(color = Modo), size = 1) +
          scale_fill_brewer(palette="Set1") +
          ggtitle(paste0(codigo(aa), "vs ", codigo(bb), ". OD: ", origen, " - ", destino)) +
          theme(
            legend.title = element_text(size = 16),
            legend.text = element_text(size = 14)
          )
        png(paste0("data/Gr/",nombre(aa), "-", nombre(bb), "-",origen,"-",destino, ".png"), width = 800, height = 800)
        print(p)
        dev.off()
        print("Si")
      } else {
        print("No")
      }
    }
  }
}

#V2
lista <- function(a,b){
  
  aplica <- F
  
  aa <- substr(a, 15, nchar(a))
  bb <- substr(b, 15, nchar(b))
  
  ta <- read.csv(paste0(a, "/", "tallies M.txt"), sep = "\t")
  tb <- read.csv(paste0(b, "/", "tallies M.txt"), sep = "\t")
  
  outliersA <- revisarOutliers(a)
  outliersB <- revisarOutliers(b)
  
  if (length(outliersA)/max(ta$Rep)<=0.5 & length(outliersB)/max(tb$Rep)<=0.5){
    
    origenes <- c(1,1,7,8)
    destinos <- c(7,8,1,1)
    for(i in 1:4){
      origen = origenes[i]
      destino = destinos[i]
      ra <- ta[ta$Origin == origen & ta$Destination == destino,]
      if (!is.null(outliersA))
        ra <- ra[!ra$Rep %in% outliersA,]
      ra <- ra[,c("Period", "Mode", "Average")]
      
      rb <- tb[tb$Origin == origen & tb$Destination == destino,]
      if (!is.null(outliersB))
        rb <- rb[!rb$Rep %in% outliersB,]
      rb <- rb[,c("Period", "Mode", "Average")]
      
      avgA <- aggregate(Average ~.,data = ra, mean)
      avgB <- aggregate(Average ~.,data = rb, mean)
      
      hwEscA <- half_width(ra[ra$Mode == "Stairs", c(1,3)])
      hwAscA <- half_width(ra[ra$Mode == "Elevator", c(1,3)])
      
      hwEscB <- half_width(rb[rb$Mode == "Stairs", c(1,3)])
      hwAscB <- half_width(rb[rb$Mode == "Elevator", c(1,3)])
      
      elevs <- data.frame()
      stairs <- data.frame()
      for(j in 0:(length(hwAscA) - 1)){
        medEscA <- avgA[avgA$Period == j & avgA$Mode == "Stairs",c("Average")]
        medAscA <- avgA[avgA$Period == j & avgA$Mode == "Elevator",c("Average")]
        
        medEscB <- avgB[avgB$Period == j & avgB$Mode == "Stairs",c("Average")]
        medAscB<- avgB[avgB$Period == j & avgB$Mode == "Elevator",c("Average")]
        
        stairs[j + 1, 1] <- "Escaleras"
        stairs[j + 1, 2] <- j
        stairs[j + 1, 3] <- overlap(medEscA, hwEscA[j + 1], medEscB, hwEscB[j + 1])
        
        elevs[j + 1, 1] <- "Ascensores"
        elevs[j + 1, 2] <- j
        elevs[j + 1, 3] <- overlap(medAscA, hwAscA[j + 1], medAscB, hwAscB[j + 1])
      }
      
      total <- rbind(stairs,elevs)
      names(total) <- c("Modo", "Periodo", "Valor")
      total$Valor <- as.numeric(total$Valor)
      #sum(total$Valor < 0, na.rm = T)/nrow(total) > 0.95 && 
      #if (sum(total$Valor == 0, na.rm = T)/nrow(total) < 0.95 & sum(total$Valor < 0, na.rm = T) < sum(total$Valor > 0, na.rm = T) ){
      if (sum(total$Valor < 0, na.rm = T) == 0 ){
        aplica = T
      } 
    }
  }
  return (aplica)
}

graficasLista <- function(a,b){
  
  aa <- substr(a, 15, nchar(a))
  bb <- substr(b, 15, nchar(b))
  
  ta <- read.csv(paste0(a, "/", "tallies M.txt"), sep = "\t")
  tb <- read.csv(paste0(b, "/", "tallies M.txt"), sep = "\t")
  
  outliersA <- revisarOutliers(a)
  outliersB <- revisarOutliers(b)
  
  repsA <- max(ta$Rep) - length(outliersA)
  repsB <- max(ta$Rep) - length(outliersB) 
  
  if (length(outliersA)/max(ta$Rep)<=0.5 & length(outliersB)/max(tb$Rep)<=0.5){
    
    origenes <- c(1,1,7,8)
    destinos <- c(7,8,1,1)
    
    grande <- data.frame()
    
    for(i in 1:4){
      origen = origenes[i]
      destino = destinos[i]
      ra <- ta[ta$Origin == origen & ta$Destination == destino,]
      if (!is.null(outliersA))
        ra <- ra[!ra$Rep %in% outliersA,]
      ra <- ra[,c("Period", "Mode", "Average", "Origin", "Destination")]
      
      rb <- tb[tb$Origin == origen & tb$Destination == destino,]
      if (!is.null(outliersB))
        rb <- rb[!rb$Rep %in% outliersB,]
      rb <- rb[,c("Period", "Mode", "Average", "Origin", "Destination")]
      
      avgA <- aggregate(Average ~.,data = ra, mean)
      avgB <- aggregate(Average ~.,data = rb, mean)
      
      hwEscA <- half_width(ra[ra$Mode == "Stairs", c(1,3)], repsA)
      hwAscA <- half_width(ra[ra$Mode == "Elevator", c(1,3)], repsB)
      
      hwEscB <- half_width(rb[rb$Mode == "Stairs", c(1,3)], repsA)
      hwAscB <- half_width(rb[rb$Mode == "Elevator", c(1,3)], repsB)
      
      elevs <- data.frame()
      stairs <- data.frame()
      for(j in 0:(length(hwAscA) - 1)){
        medEscA <- avgA[avgA$Period == j & avgA$Mode == "Stairs",c("Average")]
        medAscA <- avgA[avgA$Period == j & avgA$Mode == "Elevator",c("Average")]
        
        medEscB <- avgB[avgB$Period == j & avgB$Mode == "Stairs",c("Average")]
        medAscB<- avgB[avgB$Period == j & avgB$Mode == "Elevator",c("Average")]
        
        stairs[j + 1, 1] <- "Escaleras"
        stairs[j + 1, 2] <- j
        stairs[j + 1, 3] <- overlap(medEscA, hwEscA[j + 1], medEscB, hwEscB[j + 1])
        
        elevs[j + 1, 1] <- "Ascensores"
        elevs[j + 1, 2] <- j
        elevs[j + 1, 3] <- overlap(medAscA, hwAscA[j + 1], medAscB, hwAscB[j + 1])
      }
      
      total <- rbind(stairs,elevs)
      names(total) <- c("Modo", "Periodo", "Valor")
      total$OD <- paste0(origen,"-", destino)
      total$Valor <- as.numeric(total$Valor)
      
      grande <- rbind(grande, total)
    }
    escaleras <- grande[grande$Modo == "Escaleras", ]
    ascensores <- grande[grande$Modo == "Ascensores", ]
    
    pEsc <- ggplot(escaleras, aes(y = Valor, x = Periodo, group = OD)) +
      geom_line(aes(color = OD), size = 1) +
      scale_fill_brewer(palette="Set1") +
      ggtitle(paste0(codigo(aa), "vs ", codigo(bb), ". Modo: Escaleras")) +
      theme(
        legend.title = element_text(size = 16),
        legend.text = element_text(size = 14)
      )
    png(paste0("data/Gr/",nombre(aa), "-", nombre(bb),"-", "Escaleras", ".png"), width = 1200, height = 1200, res = 144)
    print(pEsc)
    dev.off()
    
    pAsc <- ggplot(ascensores, aes(y = Valor, x = Periodo, group = OD)) +
      geom_line(aes(color = OD), size = 1) +
      scale_fill_brewer(palette="Set1") +
      ggtitle(paste0(codigo(aa), "vs ", codigo(bb), ". Modo: Ascensores")) +
      theme(
        legend.title = element_text(size = 16),
        legend.text = element_text(size = 14)
      )
    png(paste0("data/Gr/",nombre(aa), "-", nombre(bb), "-", "Ascensores", ".png"), width = 1200, height = 1200, res = 144)
    print(pAsc)
    dev.off()
    
  }
}

#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------
#                    Compare different alternatives
#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------
library(ggplot2)
library(beepr)

alpha = 0.05
carpetas <- list.dirs("./data/output")
carpetas <- carpetas[-1]

#-----------------
# Muestra las gráficas separadas por modo de transporte para todas las alternativas vs la actual
#-----------------
for(file in carpetas){
  if(substr(file,17,17) == 4){
    for(file2 in carpetas) {
      if (file != file2){
        cat(file,file2,"\n")
        graficasLista(file,file2)
      }
    }
  }
}
beep(1)

graficasLista("./data/output/Original","./data/output/N-4_X-0_E-0_O-0_")
graficasLista("./data/output/N-4_X-0_E-0_O-0_","./data/output/N-3_X-1_E-0_O-0_")
#-----------------
# Muestra las gráficas separadas por ciertos pares de pisos.
#-----------------

# for(file in carpetas){
#   if(substr(file,17,17) == 4){
#     for(file2 in carpetas) {
#       if (file != file2){
#         comparar(file, file2)
#       }
#     }
#   }
# }

#-----------------
# Muestra las gráficas separadas por modo de transporte y solo para algunas alternativas
#-----------------
# li <- data.frame()
# for(file in carpetas){
#   if(substr(file,17,17) == 4){
#     for(file2 in carpetas) {
#       if (file != file2){
#         if (lista(file, file2)){
#           li[nrow(li) + 1, 1] <- file
#           li[nrow(li), 2] <- file2
#         }
#       }
#     }
#   }
# }
# for(i in 1:nrow(li)){
#   graficasLista(li[i,1], li[i,2])
# }


