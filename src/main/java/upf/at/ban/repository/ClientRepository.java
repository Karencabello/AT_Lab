package upf.at.ban.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import upf.at.ban.model.Client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// guarda clients per phone

public class ClientRepository{

    private static final Logger logger = LogManager.getLogger(ClientRepository.class);

    //Utilitzem Map(key(string)=num telf, value = Client)
    private static Map<String, Client> clients = new HashMap<>(); 

    private static ClientRepository INSTANCE = new ClientRepository();

    private ClientRepository() { }
    
    public static ClientRepository getInstance(){
        return INSTANCE;
    }

    public void addClient(Client client){
        clients.put(client.getPhone(), client);
        // log
        logger.info("Client subscribed phone={}", client.getPhone());
    }

    //utilitzem collection per que clients és un HashMap
    //i quan fem clients.values() es retorna un Collection<Client>
    public Collection<Client> getAllClients(){ //
        return clients.values();
    }

    public Client getClientByPhone(String phone){
        // log
        Client c = clients.get(phone);
        logger.debug("CLIENT_GET phone={} found={}", phone, (c != null));
        return c; 
    }

}