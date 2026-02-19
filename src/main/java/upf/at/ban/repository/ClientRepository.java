package upf.at.ban.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import upf.at.ban.model.Client;

// guarda clients per phone

public class ClientRepository{
    //Utilitzem Map(key(string)=num telf, value = Client)
    private static Map<String, Client> clients = new HashMap<>(); 

    public void addClient(Client client){
        clients.put(client.getPhone(), client);
    }

    //utilitzem collection per que clients és un HashMap
    //i quan fem clients.values() es retorna un Collection<Client>
    public Collection<Client> getAllClients(){ //
        return clients.values();
    }

    public Client getClientByPhone(String phone){
        return clients.get(phone); 
    }

}