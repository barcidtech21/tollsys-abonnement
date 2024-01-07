select abo.abo_nom as nom, abo.abo_prenom as prenom, abo.abo_raison_sociale as raison_sociale, abo.abo_date_creation as date_creation, sup.sup_id as support, sup.sup_immatriculation as immatriculation, abt.abt_solde as solde, sup.sup_date_passage as dernier_passage

   from pos.t_pos_abo_abonne abo,

        pos.t_pos_abt_abonnement abt,

        pos.t_pos_sup_support sup

   where abt.abo_id = abo.abo_id

     and sup.abt_id = abt.abt_id

     and abt.abt_date_resiliation is null

     and sup.ect_id = 0

     and date_trunc('day',sup.sup_date_passage) = ?

   order by sup.sup_date_passage;