#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------
#                               Funciones
#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------

#--------------------------------------------------------------------
#                       Confidence interval
#--------------------------------------------------------------------
half_width <- function(vector){
  sd <- sd(vector)
  R <- length(vector)
  error <- sd / sqrt(R)
  return(qt(1 - alpha/2, R - 1)*error)
}

#--------------------------------------------------------------------
#                         Calcular diferencias
#--------------------------------------------------------------------
diferencias <- function(df){
  numReps <- max(df$Rep)
  res <- data.frame()
  for(i in 0:151){
    contador = 1
    difs <- c()
    for(j in 0:numReps){
      temp <- df %>% filter(Period == i, Rep == j)
      if(nrow(temp) == 2){
        difs[contador] = temp[temp$Version == "OG", c("Average")] - temp[temp$Version == "ALT", c("Average")]
        contador = contador + 1 
      }
    }
    
    if (length(difs) >= 2){
      med = mean(difs)
      hw <-  half_width(difs)

      if(med - hw > 0){
        res[i+1, 1] <- med
      }
      else if(med + hw < 0){
        res[i+1, 1] <- med 
      }
      else{
        res[i+1, 1] <- 0
      }
      res[i+1,2] <- hw
      # res[i+1, 2] <- hw
      # 
      # if(mean - hw > 0){
      #   res[i+1, 2] <- "A"
      # } 
      # else if(mean + hw < 0){
      #   res[i+1, 3] <- "O"
      # }
      # else{
      #   res[i+1, 3] <- "I"
      # }
      
    }else{
      res[i+1, 1] <- mean(difs)
      res[i+1, 2] <- 0
    }
    res[i+1, 3] <- i
    #res[i+1, 4] <- i
    

  }
  #names(res) <- c("Mean", "HW", "Col", "Period")
  names(res) <- c("Val", "HW", "Period")
  return(res)
}

#--------------------------------------------------------------------
#                         Calcular diferencias
#--------------------------------------------------------------------
diferenciasChiquitas <- function(df){
  numReps <- max(df$Rep)
  res <- data.frame()
  for(i in 0:150){
    contador = 1
    difs <- c()
    for(j in 0:numReps){
      temp <- df %>% filter(Period == i, Rep == j)
      if(nrow(temp) == 2){
        difs[contador] = temp[temp$Version == "OG", c("Average")] - temp[temp$Version == "ALT", c("Average")]
        contador = contador + 1 
      }
    }
    
    if (length(difs) >= 2){
      med = mean(difs)
      hw <-  half_width(difs)
      
      res[i+1,1] <- med
      res[i+1,2] <- hw

      if(med - hw > 0){
        res[i+1, 3] <- "A"
      }
      else if(med + hw < 0){
        res[i+1, 3] <- "O"
      }
      else{
        res[i+1, 3] <- "I"
      }
      
    }else{
      res[i+1, 1] <- mean(difs)
      res[i+1, 2] <- 0
      res[i+1, 3] <- "P"
    }

    res[i+1, 4] <- i
    
    
  }
  #names(res) <- c("Mean", "HW", "Col", "Period")
  names(res) <- c("Val", "HW", "Tipo","Period")
  return(res)
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
  } else if (nchar(ruta) == 30) {
    partes <- as.data.frame(strsplit(ruta, "_"))
    asc <- as.data.frame(apply(partes, 1, function (x) strsplit(x,"-")))
    str <- paste0(asc[2,1], asc[2,2], asc[2,3], asc[2,4])
    return(str)
  } else {
    return(ruta)
  }
}

#--------------------------------------------------------------------
#                             Comparar
#--------------------------------------------------------------------

lista <- function(a,b){
  
  aa <- substr(a, 15, nchar(a))
  bb <- substr(b, 15, nchar(b))
  
  ta <- read.csv(paste0(a, "/", "tallies M.txt"), sep = "\t")
  tb <- read.csv(paste0(b, "/", "tallies M.txt"), sep = "\t")
  
  origenes <- c(1,1,7,8)
  destinos <- c(7,8,1,1)
  
  totalEsc <- data.frame()
  totalAsc <- data.frame()
  
  for(i in 1:4){
    origen = origenes[i]
    destino = destinos[i]
    ra <- ta[ta$Origin == origen & ta$Destination == destino,]
    ra <- ra[,c("Period", "Mode", "Average", "Rep")]
    ra$Version <- "OG"
    
    rb <- tb[tb$Origin == origen & tb$Destination == destino,]
    rb <- rb[,c("Period", "Mode", "Average", "Rep")]
    rb$Version <- "ALT"
    
    total <- rbind(ra,rb)
    
    avg <- aggregate(Average ~.,data = total, FUN = mean)
    
    difsEsc <- diferencias(avg %>% filter(Mode == "Stairs"))
    difsAsc <- diferencias(avg %>% filter(Mode == "Elevator"))
    
    difsEsc$OD <- paste0(origen,"-", destino)
    difsAsc$OD <- paste0(origen,"-", destino)
    
    totalEsc <- rbind(totalEsc, difsEsc)
    totalAsc <- rbind(totalAsc, difsAsc)
  }
  
  #Graficar
  pEsc <- ggplot(totalEsc, aes(y = Val, x = Period, group = OD)) +
    geom_line(aes(color = OD), size = 1) +
    scale_fill_brewer(palette="Set1") +
    ggtitle(paste0(codigo(aa), "vs ", codigo(bb), ". Modo: Escaleras")) +
    theme(
      legend.title = element_text(size = 16),
      legend.text = element_text(size = 14)
    ) +
   scale_y_continuous(labels=function(x) sprintf("%.2f", x)) +
    labs(x = "Periodo", y = "Valor")
  png(paste0("data/Gr/",nombre(aa), "-", nombre(bb),"-", "Escaleras", ".png"), width = 1200, height = 1200, res = 144)
  print(pEsc)
  dev.off()
  
  pAsc <- ggplot(totalAsc, aes(y = Val, x = Period, group = OD)) +
    geom_line(aes(color = OD), size = 1) +
    scale_fill_brewer(palette="Set1") +
    ggtitle(paste0(codigo(aa), "vs ", codigo(bb), ". Modo: Ascensores")) +
    theme(
      legend.title = element_text(size = 16),
      legend.text = element_text(size = 14)
    ) +
    scale_y_continuous(labels=function(x) sprintf("%.2f", x))+
    labs(x = "Periodo", y = "Valor")
  png(paste0("data/Gr/",nombre(aa), "-", nombre(bb),"-", "Ascensores", ".png"), width = 1200, height = 1200, res = 144)
  print(pAsc)
  dev.off()
}

#--------------------------------------------------------------------
#                           Partir las gráficas
#--------------------------------------------------------------------

chiquitos <- function(a,b){
  
  aa <- substr(a, 15, nchar(a))
  bb <- substr(b, 15, nchar(b))
  
  ta <- read.csv(paste0(a, "/", "tallies M.txt"), sep = "\t")
  tb <- read.csv(paste0(b, "/", "tallies M.txt"), sep = "\t")
  
  origenes <- c(1,1,7,8)
  destinos <- c(7,8,1,1)
  
  totalEsc <- data.frame()
  totalAsc <- data.frame()
  
  for(i in 1:1){
    origen = origenes[i]
    destino = destinos[i]
    ra <- ta[ta$Origin == origen & ta$Destination == destino,]
    ra <- ra[,c("Period", "Mode", "Average", "Rep")]
    ra$Version <- "OG"
    
    rb <- tb[tb$Origin == origen & tb$Destination == destino,]
    rb <- rb[,c("Period", "Mode", "Average", "Rep")]
    rb$Version <- "ALT"
    
    total <- rbind(ra,rb)
    
    avg <- aggregate(Average ~.,data = total, FUN = mean)
    
    difsEsc <- diferenciasChiquitas(avg %>% filter(Mode == "Stairs"))
    difsAsc <- diferenciasChiquitas(avg %>% filter(Mode == "Elevator"))
    
    difsEsc$OD <- paste0(origen,"-", destino)
    difsAsc$OD <- paste0(origen,"-", destino)
    
    totalEsc <- rbind(totalEsc, difsEsc)
    totalAsc <- rbind(totalAsc, difsAsc)
  }
  
  #Graficar
  pEsc <- ggplot(totalAsc, aes(y = Val, x = Period, group = OD, color = OD)) +
    #geom_line(color = "red", size = 1) +
    geom_point()+
    geom_errorbar(aes(ymin=Val - HW, ymax=Val + HW), width=.2, position=position_dodge(0.05), color = "black") +
    ggtitle(paste0(codigo(aa), "vs ", codigo(bb), ". Modo: Ascensores")) +
    theme(
      legend.title = element_text(size = 16),
      legend.text = element_text(size = 14)) +
    scale_y_continuous(labels=function(x) sprintf("%.2f", x)) +
    labs(x = "Periodo", y = "Intervalo de confianza")
  png(paste0("data/Gr/chiquitas.png"), width = 1200, height = 1200, res = 144)
  print(pEsc)
  dev.off()
}
#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------
#                    Compare different alternatives
#-------------------------------------------------------------------------------
#-------------------------------------------------------------------------------
#setwd("C:/Users/dunca/OneDrive - Universidad de los andes/TESIS/Java/Vertical Flow SD Otero ASC")
library(ggplot2)
library(beepr)
library(dplyr)

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
        lista(file,file2)
      }
    }
  }
}
beep(1)

f1 = "./data/output/Original"
for(f2 in c("piso 1", "piso 7", "piso 8", "piso 7 8", "piso 1 7 8")){
  f2n = paste0("./data/output/", f2)
  cat(f1,f2n,"\n")
  lista(f1, f2n)
}

lista("./data/output/N-4_X-0_E-0_O-0_","./data/output/Modificado")
a = "./data/output/N-4_X-0_E-0_O-0_"
b = "./data/output/N-2_X-0_E-0_O-2_"
chiquitos(a,b)
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


